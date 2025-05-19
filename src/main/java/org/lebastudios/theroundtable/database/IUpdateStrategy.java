package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IUpdateStrategy
{
    IUpdateStrategy[] STRATEGIES = new IUpdateStrategy[]{
            new MethodUpdateStrategy(),
            new SQLFileUpdateStrategy()
    };

    boolean upgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception;

    boolean downgradeTo(Connection conn, int version, IPlugin plugin, Dbms dbms) throws Exception;

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

            for (ISQLTransformer transformer : ISQLTransformer.TRANSFORMERS)
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
    }
}
