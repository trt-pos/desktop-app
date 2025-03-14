package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class DatabaseUpdater
{
    private static DatabaseUpdater instance;

    public static DatabaseUpdater getInstance()
    {
        if (instance == null) instance = new DatabaseUpdater();

        return instance;
    }

    private DatabaseUpdater() {}

    public void callToUpdate(Connection conn) throws SQLException
    {
        conn.setAutoCommit(false);
        var plugins = PluginLoader.getLoadedPlugins();

        // Create the database version table if it doesn't exist
        try
        {
            String sql = """
                    create table core_database_version
                    (
                        plugin_identifier varchar(255) not null
                            primary key,
                        version           integer
                    );
                    """;
            conn.createStatement().execute(sql);
        }
        catch (SQLException exception)
        {
            // Ignore if the table already exists
            if (!exception.getMessage().contains("already exists")) throw exception;
        }

        try
        {
            // Update the database version for the core
            updateDatabaseFor(conn, new DesktopAppDatabaseUpdater());

            // Update the database version for each plugin
            for (var plugin : plugins)
            {
                updateDatabaseFor(conn, plugin);
            }
        }
        catch (Exception e)
        {
            Logs.getInstance().log(Logs.LogType.ERROR, "The database update couldn't be complited");
            throw new RuntimeException(e);
        }
    }

    private void updateDatabaseFor(Connection conn, IDatabaseUpdater updater) throws Exception
    {
        var identifier = updater.getDatabaseIdentifier();
        var newVersion = updater.getDatabaseVersion();

        String sql = """
                select version from core_database_version where plugin_identifier = ?
                """;

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, identifier);


        int oldVersion = 0;
        boolean exists = false;
        
        var result = statement.executeQuery();

        if (result.next())
        {
            oldVersion = result.getInt(1);
            exists = true;
        }

        statement.close();

        if (oldVersion < newVersion)
        {
            try
            {
                conn.setAutoCommit(false);
                
                updater.updateDatabase(conn, oldVersion, newVersion);

                sql = String.format(
                        exists
                                ? "update core_database_version set version = %d where plugin_identifier = '%s'"
                                : "insert into core_database_version (version, plugin_identifier) values (%d, '%s')",
                        newVersion, identifier);

                conn.createStatement().executeUpdate(sql);
                conn.commit();
            }
            catch (SQLException e)
            {
                Logs.getInstance().log("Error updating database for " + identifier, e);
                conn.rollback();
                throw new Exception("Error updating the database");
            }
        }
    }
}
