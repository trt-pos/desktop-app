package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// # Database Updater
/// The Database Updater interface is a piece of software that allows the maintainance of a multi dbms sytem with ease.
/// It provides a way to update the database schema and data using a set of strategies and macros that we will explain
/// as we go along.
///
/// ## Supported ways to update the database
///
/// ## SQL Files Macros
/// When writting the SQL files, you can use the following macros to apply transformations to the SQL that make it
/// compatible with all the supported DBMS.
///
/// ### Autoincrement
/// When creating a table, you can use the `AUTOINCREMENT|autoincrement` keyword to define a column as autoincrement.
/// The keyword will be replaced by the correct keyword for the DBMS.
///
/// Note that for sqlite, the column must be the primary key and the constraint should be defined inline. For example:
/// ```sql
/// create table my_table (
///    id integer primary key autoincrement,
///    name varchar(255)
///);
///```
///
/// ### Migrator
/// As many people know, SQLite does not support the `ALTER TABLE` statement as
/// other DBMS do. So, in order to alter a table that SQLITE does not support, we
/// need to create a new table and copy the data from the old table to the new
/// table, then drop the old and rename the new one.
///
/// This is too much boilerplate code, so we created a macro that will do this for you.
/// ```sql
/// -- MIGRATE
/// create table my_table (
///     id integer primary key autoincrement,
///     name varchar(255),
///     age integer,
///     -- NEW
///     new_column varchar(255),
///     constraint unique_name unique(name)
///);
///```
///
/// The macro creates a table as the defined one in the example above with the
/// name ended with `_tmp`. Then copies the columns that are above the `-- NEW`,
/// drops the old table and renames the new one to `my_table`.
///
/// Note: This macro doesn't rename existing columns and cannot convert
/// beetwen types. It only copies the columns that are above the `-- NEW`
/// flag. Note that you can delete one column just by ignoring it.
///
/// ### Conditional DBMS
/// Sometimes we need to execute a SQL statement only for a specific DBMS but
/// the rest of the SQL is compatible with all the DBMS. For this, we can use the
/// `-- IF <DBMS>` macro.
///
/// ```sql
/// -- IF SQLITE
/// create trigger core_app_installation_ensure_single_master_before_update
///     before update
///     on core_app_installation
///     for each row
///     when new.is_master = 1 and old.is_master <> 1
/// begin
///     select raise(abort, 'only one row can have is_active = 1')
///     where exists (select 1 from core_app_installation where is_master = 1 and uuid != old.uuid);
/// end;
/// -- ENDIF
///
/// -- IF MARIADB
/// create trigger core_app_installation_ensure_single_master_before_update
///     before update
///     on core_app_installation
///     for each row
/// begin
///     if new.is_master = 1 and old.is_master <> 1 then
///         if (select count(*) from core_app_installation where is_master = 1 and uuid != old.uuid) > 0 then
///             signal sqlstate '45000'
///                 set message_text = 'only one row can have is_master = 1';
///         end if;
///     end if;
/// end;
/// -- ENDIF
///```
public interface IDatabaseUpdater
{
    default int getDatabaseVersion() {return 0;}

    default void upgradeDatabaseTo(Connection conn, int version, Dbms dbms) throws Exception
    {
        Class<? extends IPlugin> clazz =
                PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();
        IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();

        for (IUpdateStrategy strategy : IUpdateStrategy.STRATEGIES)
        {
            if (strategy.upgradeTo(conn, version, plugin, dbms))
            {
                return;
            }
        }

        throw new Exception("No update strategy found when upgrading db to version " + version);
    }

    default void downgradeDatabaseTo(Connection conn, int version, Dbms dbms) throws Exception
    {
        Class<? extends IPlugin> clazz =
                PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();
        IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();

        for (IUpdateStrategy strategy : IUpdateStrategy.STRATEGIES)
        {
            if (strategy.downgradeTo(conn, version, plugin, dbms))
            {
                return;
            }
        }

        throw new Exception("No update strategy found when downgrading db to version " + version);
    }

    interface IUpdateStrategy
    {
        IUpdateStrategy[] STRATEGIES = new IUpdateStrategy[]{
                new MethodUpdateStrategy(),
                new SQLFileUpdateStrategy()
        };

        boolean upgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception;

        boolean downgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception;
    }

    class MethodUpdateStrategy implements IUpdateStrategy
    {
        @Override
        public boolean upgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception
        {
            return executeMethod("upgradeTo" + version, conn, plugin, dbms);
        }

        @Override
        public boolean downgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception
        {
            return executeMethod("downgradeTo" + version, conn, plugin, dbms);
        }

        private boolean executeMethod(String name, Connection conn, IPlugin plugin, Dbms dbms)
                throws InvocationTargetException, IllegalAccessException
        {
            Method[] methods = plugin.getClass().getDeclaredMethods();

            var updateMethod = Arrays.stream(methods)
                    .filter(method -> method.getName().equals(name))
                    .findFirst();

            if (updateMethod.isEmpty()) return false;

            updateMethod.get().invoke(plugin, conn, dbms);

            return true;
        }
    }

    class SQLFileUpdateStrategy implements IUpdateStrategy
    {
        private static final String DELIMITER = "-- DELIMITER";

        private enum UpdateType
        {
            UPGRADE,
            DOWNGRADE
        }

        @Override
        public boolean upgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception
        {
            return executeMethod(UpdateType.UPGRADE, conn, version, plugin, dbms);
        }

        @Override
        public boolean downgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception
        {
            return executeMethod(UpdateType.DOWNGRADE, conn, version, plugin, dbms);
        }

        private boolean executeMethod(UpdateType updateType, Connection conn, int version, IPlugin plugin, Dbms dbms)
                throws Exception
        {
            try (InputStream is = getSQLFile(dbms, updateType, version, plugin.getClass()))
            {
                executeSQL(conn, is, dbms, plugin);
            }

            return true;
        }

        private InputStream getSQLFile(Dbms dbms, UpdateType updateType, int version, Class<? extends IPlugin> clazz)
                throws Exception
        {
            // Finding for a generic SQL file first
            String fileName = "sql/" + updateType.name().toLowerCase() + "/to-" + version + ".sql";
            InputStream is = clazz.getResourceAsStream(fileName);

            if (is != null) return is;

            // If not found, try to find a DBMS specific SQL file
            fileName = "sql/" + updateType.name().toLowerCase() + "/" + dbms.name().toLowerCase() + "-to-" + version +
                    ".sql";
            is = clazz.getResourceAsStream(fileName);

            if (is != null) return is;

            throw new FileNotFoundException("Update sql file into version " + version + " not found.\n"
                    + "The should be a file named as one of this options:\n"
                    + "\tsql/<upgrade|downgrade>/to-<version>.sql\n"
                    + "\tsql/<upgrade|downgrade>/<dbms>-to-<version>.sql ");
        }

        private void executeSQL(Connection conn, InputStream is, Dbms dbms, IPlugin plugin) throws SQLException, IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Statement statement = conn.createStatement();

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isBlank()) continue;

                if (line.trim().startsWith(DELIMITER))
                {
                    executeStatement(statement, sql.toString(), dbms, plugin);

                    sql.setLength(0);
                    continue;
                }

                sql.append(line).append("\n");
            }

            executeStatement(statement, sql.toString(), dbms, plugin);
        }

        private void executeStatement(Statement statement, String sql, Dbms dbms, IPlugin plugin) throws SQLException
        {
            if (sql.trim().isBlank()) return;

            List<String> sqlList = List.of(sql);

            for (ISQLMacroHandler transformer : ISQLMacroHandler.TRANSFORMERS)
            {
                List<String> newSqlList = new ArrayList<>();

                for (String sqlStr : sqlList)
                {
                    newSqlList.addAll(transformer.transform(sqlStr, dbms, plugin));
                }

                sqlList = newSqlList;
            }

            for (String sqlStr : sqlList)
            {
                Logs.getInstance().log(
                        Logs.LogType.DEBUG,
                        "Executing SQL statement:\n" + sqlStr
                );
                statement.execute(sqlStr);
            }
        }

        private interface ISQLMacroHandler
        {
            ISQLMacroHandler[] TRANSFORMERS = new ISQLMacroHandler[]{
                    new ConditionalDBMSMacroHandler(),
                    new AutoincrementMacroHandler(),
                    new TableMigratorMacroHandler(),
            };

            List<String> transform(String sql, Dbms dbms, IPlugin plugin);

            default String replaceFirstGroup(String regex, String replacement, String text)
            {
                StringBuilder modifiedSql = new StringBuilder();

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                int lastMatchEnd = 0;
                while (matcher.find())
                {
                    modifiedSql.append(text, lastMatchEnd, matcher.start(1));
                    modifiedSql.append(replacement);
                    lastMatchEnd = matcher.end(1);
                }

                modifiedSql.append(text, lastMatchEnd, text.length());
                return modifiedSql.toString();
            }
        }

        private static class AutoincrementMacroHandler implements ISQLMacroHandler
        {
            @Override
            public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
            {
                if (sql.contains("CREATE TABLE") || sql.contains("create table"))
                {
                    String replacement = switch (dbms)
                    {
                        case SQLITE -> "autoincrement";
                        case MARIADB -> "auto_increment";
                    };

                    sql = this.replaceFirstGroup(" ((?i)AUTOINCREMENT)[ ,;]", replacement, sql);
                }

                return List.of(sql);
            }
        }

        private static class TableMigratorMacroHandler implements ISQLMacroHandler
        {
            private final static String regex =
                    "[ \n]*(?:(?i)create table) (\\w*)[^(]*\\([ \n]*((?:\\w* [^,].*\n)+)[ \n]*-- NEW";
            private static final Pattern pattern = Pattern.compile(regex);


            @Override
            public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
            {
                List<String> sqlList = new ArrayList<>();

                if (sql.trim().startsWith("-- MIGRATE"))
                {
                    Matcher matcher = pattern.matcher(sql);

                    while (matcher.find())
                    {
                        String tableName = matcher.group(1);
                        String oldColDef = matcher.group(2);

                        String tmpTableName = tableName + "_tmp";
                        String deletedTableName = tableName + "_old";
                        List<String> colsNames = extractColsNames(oldColDef);

                        // Creating the tmp table
                        sqlList.add(this.replaceFirstGroup(regex, tmpTableName, sql));

                        // Copy data if exists
                        sqlList.add("insert into " + tmpTableName + " (" + String.join(",", colsNames) +
                                ") " + "select " + String.join(",", colsNames) + " from " + tableName + ";");

                        // dropping the old table and renaming the new one
                        sqlList.add("drop table if exists " + tableName + ";");
                        sqlList.add("alter table " + tmpTableName + " rename to " + tableName + ";");
                    }
                }
                else
                {
                    sqlList.add(sql);
                }

                return sqlList;
            }

            private List<String> extractColsNames(String createTableBody)
            {
                List<String> colsNames = new ArrayList<>();

                for (String line : createTableBody.split(", *\\R"))
                {
                    String trimmedLine = line.trim();
                    if (trimmedLine.isEmpty()) continue;

                    colsNames.add(trimmedLine.substring(0, trimmedLine.indexOf(" ")));
                }

                return colsNames;
            }
        }

        private static class ConditionalDBMSMacroHandler implements ISQLMacroHandler
        {
            @Override
            public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
            {
                Pattern pattern = Pattern.compile("(?si)-- IF " + dbms.name() + " *\\R(.*?)-- ENDIF");
                List<String> sqlList = new ArrayList<>();

                Matcher matcher = pattern.matcher(sql);
                if (matcher.find())
                {
                    String conditionBody = matcher.group(1);
                    sqlList.add(conditionBody);
                }
                else
                {
                    sqlList.add(sql);
                }

                return sqlList;
            }
        }
    }
}
