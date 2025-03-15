package org.lebastudios.theroundtable.database;

import lombok.AllArgsConstructor;
import org.lebastudios.theroundtable.logs.Logs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
class DatabaseMigrator
{
    private Connection from;
    private Connection to;

    public Exception migrate()
    {
        try
        {
            from.setAutoCommit(false);
            to.setAutoCommit(false);

            new CoreMigrationStrategy().migrate();

            from.commit();
            to.commit();

            return null;
        }
        catch (SQLException exception)
        {
            try
            {
                from.rollback();
                to.rollback();
            }
            catch (Exception e)
            {
                Logs.getInstance().log("Error trying to execute rollback after error migrating", e);
            }

            Logs.getInstance().log("Error while trying to migrate the database", exception);
            return exception;
        }
    }

    private PreparedStatement createInsertStatementForTable(Table table) throws SQLException
    {
        StringBuilder insertValues = new StringBuilder();
        StringBuilder inserParams = new StringBuilder();

        for (String col : table.columns)
        {
            insertValues.append(col).append(",");
            inserParams.append("?,");
        }

        insertValues.deleteCharAt(insertValues.length() - 1);
        inserParams.deleteCharAt(inserParams.length() - 1);

        String preparedInsert = "insert into " + table.name + "(" + insertValues + ")" +
                " values (" + inserParams + ")";

        return to.prepareStatement(preparedInsert);
    }

    private PreparedStatement createSelectStatementForTable(Table table) throws SQLException
    {
        StringBuilder selectValues = new StringBuilder();

        for (String col : table.columns)
        {
            selectValues.append(col).append(",");
        }

        selectValues.deleteCharAt(selectValues.length() - 1);

        String preparedSelect = "select " + selectValues + " from " + table.name;

        return from.prepareStatement(preparedSelect);
    }

    private static boolean isTableEmpty(String tableName, Connection conn) throws SQLException
    {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select count(*) from " + tableName))
        {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }
    
    private record Table(String name, List<String> columns)
    {
        private static final List<String> ignoredMariaDbTables = List.of(
                "global_status",
                "session_account_connect_attrs",
                "session_status"
        );

        public static List<Table> introspectTables(DatabaseMetaData metadata, String tableNamePattern) throws SQLException
        {
            List<Table> tables = new ArrayList<>();

            try (ResultSet rs = metadata.getTables(null, null, tableNamePattern, new String[]{"TABLE"}))
            {
                while (rs.next())
                {
                    final String tableName = rs.getString("TABLE_NAME");
                    
                    if (metadata.getDatabaseProductName().equals("MariaDB")
                            && ignoredMariaDbTables.contains(tableName))
                    {
                        continue;
                    }

                    tables.add(new Table(tableName, introspectColumns(tableName, metadata)));
                }
            }

            return tables;
        }

        public static List<String> introspectColumns(String tableName, DatabaseMetaData metadata) throws SQLException
        {
            List<String> cols = new ArrayList<>();

            try (ResultSet rs = metadata.getColumns(null, null, tableName, "%"))
            {
                while (rs.next())
                {
                    cols.add(rs.getString("COLUMN_NAME"));
                }
            }

            return cols;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == null || getClass() != o.getClass()) return false;

            Table table = (Table) o;
            return name.equals(table.name);
        }

        @Override
        public int hashCode()
        {
            return name.hashCode();
        }
    }
    
    private interface IMigratorStrategy
    {
        void migrate() throws SQLException;
    }

    private class CoreMigrationStrategy implements IMigratorStrategy
    {
        @Override
        public void migrate() throws SQLException
        {
            DatabaseMetaData fromMetadata = from.getMetaData();
            DatabaseMetaData toMetadata = to.getMetaData();

            List<Table> fromTables = Table.introspectTables(fromMetadata, "core_%");

            if (toMetadata.getDatabaseProductName().equals("MariaDB"))
            {
                to.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");
            }

            for (Table table : fromTables)
            {
                // This table gets created and updated when initializing hibernate
                if (!isTableEmpty(table.name, to)) continue;

                try (PreparedStatement select = createSelectStatementForTable(table);
                     ResultSet rs = select.executeQuery();
                     PreparedStatement insert = createInsertStatementForTable(table))
                {
                    while (rs.next())
                    {
                        for (int i = 0; i < table.columns.size(); i++)
                        {
                            insert.setObject(i + 1, rs.getObject(i + 1));
                        }

                        insert.execute();
                    }
                }
            }

            if (toMetadata.getDatabaseProductName().equals("MariaDB"))
            {
                to.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
    }
}
