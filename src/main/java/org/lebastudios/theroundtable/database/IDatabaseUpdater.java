package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IDatabaseUpdater
{
    default int getDatabaseVersion() {return 0;}

    default void updateDatabase(Connection conn, int oldVersion, int newVersion, Dbms dbms) throws Exception
    {
        if (oldVersion == newVersion) return;

        Class<? extends IPlugin> clazz =
                PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();

        if (new MethodUpdateStrategy().updateDatabase(conn, oldVersion, newVersion, clazz, dbms)) return;
        if (new SQLFileUpdateStrategy().updateDatabase(conn, oldVersion, newVersion, clazz, dbms)) return;

        throw new Exception("No update strategy found when updating from version " + oldVersion + " to " + newVersion);
    }

    interface IUpdateStrategy
    {
        boolean updateDatabase(Connection conn, int oldVersion, int newVersion,
                Class<? extends IPlugin> clazz, Dbms dbms) throws Exception;
    }

    class MethodUpdateStrategy implements IUpdateStrategy
    {
        @Override
        public boolean updateDatabase(Connection conn, int oldVersion, int newVersion, Class<? extends IPlugin> clazz,
                Dbms dbms)
                throws Exception
        {
            IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();
            Method[] methods = clazz.getDeclaredMethods();
            Optional<Method> someMethod = Arrays.stream(methods)
                    .filter(method -> method.getName().startsWith("downgradeTo") ||
                            method.getName().startsWith("upgradeTo"))
                    .findFirst();

            if (someMethod.isEmpty()) return false;

            for (int i = oldVersion - 1; i >= newVersion; i--)
            {
                var methodName = "downgradeTo" + i;
                var updateMethod = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst();

                if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

                updateMethod.get().invoke(plugin, conn, dbms);
            }

            for (int i = oldVersion + 1; i <= newVersion; i++)
            {
                var methodName = "upgradeTo" + i;
                var updateMethod = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst();

                if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

                updateMethod.get().invoke(plugin, conn, dbms);
            }

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
        public boolean updateDatabase(Connection conn, int oldVersion, int newVersion, Class<? extends IPlugin> clazz,
                Dbms dbms)
                throws Exception
        {
            URL upgradeURL = clazz.getResource("sql/" + UpdateType.UPGRADE.name().toLowerCase());
            URL downgradeURL = clazz.getResource("sql/" + UpdateType.DOWNGRADE.name().toLowerCase());

            if (upgradeURL == null || downgradeURL == null) return false;

            for (int i = oldVersion - 1; i >= newVersion; i--)
            {
                try (InputStream is = getSQLFile(dbms, UpdateType.DOWNGRADE, i, clazz))
                {
                    executeSQL(conn, is, dbms);
                }
            }

            for (int i = oldVersion + 1; i <= newVersion; i++)
            {
                try (InputStream is = getSQLFile(dbms, UpdateType.UPGRADE, i, clazz))
                {
                    executeSQL(conn, is, dbms);
                }
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

        private void executeSQL(Connection conn, InputStream is, Dbms dbms) throws SQLException, IOException
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
                    executeStatement(statement, sql.toString(), dbms);

                    sql.setLength(0);
                    continue;
                }

                sql.append(line).append("\n");
            }

            executeStatement(statement, sql.toString(), dbms);
        }

        private void executeStatement(Statement statement, String sql, Dbms dbms) throws SQLException
        {
            if (sql.trim().isBlank()) return;

            List<String> sqlList = List.of(sql);

            for (ISQLTransformer transformer : ISQLTransformer.transformers)
            {
                List<String> newSqlList = new ArrayList<>();

                for (String sqlStr : sqlList)
                {
                    newSqlList.addAll(transformer.transform(sqlStr, dbms));
                }

                sqlList = newSqlList;
            }

            for (String sqlStr : sqlList)
            {
                statement.execute(sqlStr);
            }
        }

        private interface ISQLTransformer
        {
            ISQLTransformer[] transformers = new ISQLTransformer[]{
                    new AutoincrementTransformer(),
                    new TableMigratorTransformer()
            };

            List<String> transform(String sql, Dbms dbms);

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

        private static class AutoincrementTransformer implements ISQLTransformer
        {
            @Override
            public List<String> transform(String sql, Dbms dbms)
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

        private static class TableMigratorTransformer implements ISQLTransformer
        {
            private final static String regex =
                    "[ \n]*(?:(?i)create table) (\\w*)[^(]*\\([ \n]*((?:\\w* [^,].*\n)+)[ \n]*-- NEW";
            private static final Pattern pattern = Pattern.compile(regex);


            @Override
            public List<String> transform(String sql, Dbms dbms)
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
                        List<String> colsNames = extractColsNames(oldColDef);

                        // Creating the tmp table
                        sqlList.add(this.replaceFirstGroup(regex, tmpTableName, sql));

                        // Copy data if exists
                        sqlList.add("insert into " + tmpTableName + " (" + String.join(",", colsNames) +
                                ") " + "select " + String.join(",", colsNames) + " from " + tableName);
                        
                        // Drop the old table and renaming the new one
                        sqlList.add("drop table " + tableName);
                        sqlList.add("alter table " + tmpTableName + " rename to " + tableName);
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

                for (String line : createTableBody.split(","))
                {
                    String trimmedLine = line.trim();
                    if (trimmedLine.isEmpty()) continue;

                    colsNames.add(trimmedLine.substring(0, trimmedLine.indexOf(" ")));
                }

                return colsNames;
            }
        }
    }
}
