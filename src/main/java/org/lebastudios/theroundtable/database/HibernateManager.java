package org.lebastudios.theroundtable.database;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.lebastudios.theroundtable.config.DatabaseConfigData;
import org.lebastudios.theroundtable.database.entities.Account;
import org.lebastudios.theroundtable.database.entities.DatabaseVersion;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.events.DatabaseEvents;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.tasks.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

class HibernateManager
{
    private static HibernateManager instance;

    private SessionFactory sessionFactory;

    public static HibernateManager getInstance()
    {
        if (instance == null) instance = new HibernateManager();

        return instance;
    }

    private HibernateManager() {}
    
    public Task<Void> initTask()
    {
        return new InitDatabaseTask();
    }
    
    public Task<Void> reloadTask()
    {
        return new ReloadDatabaseTask();
    }
    
    public boolean connectTransaction(Consumer<Session> action)
    {
        if (sessionFactory == null) throw new IllegalStateException("Database not initialized");

        Session session = sessionFactory.openSession();

        try
        {
            session.getTransaction().begin();

            action.accept(session);

            if (session.getTransaction().isActive())
            {
                session.getTransaction().commit();
            }
        }
        catch (Exception e)
        {
            Logs.getInstance().log("Hibernate transaction failed.", e);
            if (session.getTransaction().isActive())
            {
                session.getTransaction().rollback();
            }
            return false;
        }
        finally
        {
            session.close();
        }

        return true;
    }

    public <R> R connectQuery(Function<Session, R> action)
    {
        if (sessionFactory == null) throw new IllegalStateException("Database not initialized");

        // Ejemplo de uso de la sesión para interactuar con la base de datos
        try (Session session = sessionFactory.openSession())
        {
            return action.apply(session);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("Hibernate query failed.", e);
            return null;
        }
    }
    
    private class InitDatabaseTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            if (sessionFactory != null) return null;

            sessionFactory = executeSubtask(new BuildSessionFactoryTask());
            DatabaseEvents.onDatabaseInit.invoke();

            AppLifeCicleEvents.OnAppClose.addListener((_) ->
            {
                DatabaseEvents.onDatabaseClose.invoke();
                sessionFactory.close();
            });
            
            return null;
        }
    }
    
    private class ReloadDatabaseTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Reloading database");
            
            updateMessage("Closing database");
            DatabaseEvents.onDatabaseClose.invoke();
            sessionFactory.close();
            updateProgress(0.5, 1);
            
            updateMessage("Starting database");
            sessionFactory = executeSubtask(new BuildSessionFactoryTask());
            DatabaseEvents.onDatabaseInit.invoke();
            updateProgress(1, 1);
            return null;
        }
    }
    
    private static class BuildSessionFactoryTask extends Task<SessionFactory>
    {
        @Override
        protected SessionFactory call() throws Exception
        {
            updateTitle("Starting database connections");
            
            DatabaseConfigData databaseConfigData = new DatabaseConfigData().load();
            executeSubtask(new PrepareDatabaseTask(databaseConfigData));
            
            updateMessage("Building database configuration");
            updateProgress(50, 100);
            var config = databaseConfigData.getHibernateConf();

            config.addAnnotatedClass(Account.class)
                    .addAnnotatedClass(DatabaseVersion.class);

            // Loading all the plugin entities to the Hibernate configuration from the Plugins
            updateMessage("Adding plugins to the database configuration");
            PluginsManager.getInstance().getPluginDatabaseEntities().forEach(config::addAnnotatedClass);

            // Adding the plugin ClassLoader to the Hibernate configuration
            StandardServiceRegistry serviceRegistry =
                    new StandardServiceRegistryBuilder(
                            new BootstrapServiceRegistryBuilder().applyClassLoader(PluginLoader.getPluginsClassLoader())
                                    .build())
                            .applySettings(config.getProperties())
                            .build();

            updateProgress(0.75, 1);
            updateMessage("Applying database configuration");
            return config.buildSessionFactory(serviceRegistry);
        }
    }

    @AllArgsConstructor
    private static class PrepareDatabaseTask extends Task<Void>
    {
        private final DatabaseConfigData databaseConfigData;

        @Override
        protected Void call() throws Exception
        {
            updateTitle("Preparing database");
            try (Connection conn = databaseConfigData.getConnection())
            {
                updateMessage("Updating database");
                updateProgress(0, 1);
                executeSubtask(new DatabaseUpdateTask(conn));
                updateProgress(1, 1);
                return null;
            }
        }
    }

    @AllArgsConstructor
    private static class DatabaseUpdateTask extends Task<Void>
    {
        private final Connection conn;

        @Override
        protected Void call() throws Exception
        {
            updateTitle("Updating database structure");
            updateMessage("Reading loaded plugins");
            updateProgress(0, 1);
            conn.setAutoCommit(false);

            // Create the database version table if it doesn't exist

            if (!conn.getMetaData().getTables(
                    null,
                    null,
                    "core_database_version",
                    new String[]{"TABLE"}).next()
            )
            {
                updateMessage("Creating version managment table");
                String sql = """
                        create table core_database_version
                        (
                            plugin_identifier varchar(255) not null primary key,
                            version           integer
                        );
                        """;
                conn.createStatement().execute(sql);
            }

            updateMessage("Ask each plugin to update");
            // Update the database version for the core
            updateDatabaseFor(conn, "desktop-app", new DesktopAppDatabaseUpdater());
            
            // Update the database version for each plugin
            var plugins = PluginsManager.getInstance().getLoadedPlugins();
            int i = 0;
            for (var plugin : plugins)
            {
                updateDatabaseFor(conn, plugin.getPluginData().pluginId, plugin);
                i++;
                updateProgress(i, plugins.size());
            }
            
            return null;
        }

        private void updateDatabaseFor(Connection conn, String identifier, IDatabaseUpdater updater) throws Exception
        {
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

            if (oldVersion != newVersion)
            {
                try
                {
                    conn.setAutoCommit(false);

                    updater.updateDatabase(conn, oldVersion, newVersion);

                    sql = String.format(
                            exists
                                    ? "update core_database_version set version = %d where plugin_identifier = '%s'"
                                    :
                                    "insert into core_database_version (version, plugin_identifier) values (%d, '%s')",
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
}
