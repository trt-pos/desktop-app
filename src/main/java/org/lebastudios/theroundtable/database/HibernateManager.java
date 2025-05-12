package org.lebastudios.theroundtable.database;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.config.DatabaseConfigData;
import org.lebastudios.theroundtable.config.DatabaseConfigPaneController;
import org.lebastudios.theroundtable.config.RequestConfigStageController;
import org.lebastudios.theroundtable.database.entities.AppInstallation;
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
import java.util.function.Supplier;

class HibernateManager
{
    private static HibernateManager instance;

    static
    {
        AppLifeCicleEvents.OnAppShutdown.addListener(() ->
        {
            if (instance.sessionFactory == null) return;

            Database.getInstance().connectTransaction(session ->
            {
                AppInstallation appInstallation = AppInstallation.thisInstalation(session);

                if (appInstallation.getStatus() != AppInstallation.Status.DISABLED)
                {
                    appInstallation.setStatus(AppInstallation.Status.INACTIVE);
                }

                session.merge(appInstallation);
            });

            DatabaseEvents.onDatabaseClose.invoke();
            instance.sessionFactory.close();
        });
    }

    private SessionFactory sessionFactory;
    private Connection connection;

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

    public Session getSession()
    {
        if (sessionFactory == null) throw new IllegalStateException("Database not initialized");

        try
        {
            connection.createStatement().execute("SELECT 1");
        }
        catch (SQLException exception)
        {
            try
            {
                connection.close();
            }
            catch (SQLException ignore) {}
            TheRoundTableApplication.executeInFxThreadAndWait(() ->
            {
                new RequestConfigStageController(new DatabaseConfigPaneController())
                        .setTitle("Connection with the database lost")
                        .instantiate(true);
            });

            return getSession();
        }

        return sessionFactory.openSession();
    }

    public boolean connectTransaction(Consumer<Session> action)
    {
        Session session = getSession();

        try
        {
            session.getTransaction().begin();

            action.accept(session);

            if (session.getTransaction().isActive())
            {
                session.getTransaction().commit();
                return true;
            }
        }
        catch (Exception e)
        {
            Logs.getInstance().log(
                    "Error during transaction",
                    e
            );
            if (session.getTransaction().isActive())
            {
                session.getTransaction().rollback();
            }
        }
        finally
        {
            session.close();
        }

        return false;
    }

    public <R> R connectQuery(Function<Session, R> action)
    {
        try (Session session = getSession())
        {
            return action.apply(session);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("Hibernate query failed.", e);
            return null;
        }
    }

    private class StopDatabaseTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Stopping database");

            if (sessionFactory != null)
            {
                updateMessage("Closing database");
                DatabaseEvents.onDatabaseClose.invoke();
                sessionFactory.close();
                sessionFactory = null;
            }

            return null;
        }
    }

    private class InitDatabaseTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Starting database");

            if (sessionFactory != null) return null;

            updateMessage("Starting database");
            try
            {
                sessionFactory = executeSubtask(new BuildSessionFactoryTask());
                connection = new DatabaseConfigData().load().getConnection();
                DatabaseEvents.onDatabaseInit.invoke();
            }
            catch (Exception e)
            {
                Logs.getInstance().log(
                        "Error connecting to the database",
                        e
                );
                TheRoundTableApplication.executeInFxThreadAndWait(() ->
                {
                    new RequestConfigStageController(new DatabaseConfigPaneController())
                            .setTitle("Invalid database configuration")
                            .instantiate(true);
                });
            }

            return null;
        }
    }

    private class ReloadDatabaseTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Reloading database");

            executeSubtask(new StopDatabaseTask());
            updateProgress(0.5, 1);

            executeSubtask(new InitDatabaseTask());
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

            // Loading all the plugin entities to the Hibernate configuration from the Plugins
            updateMessage("Adding plugins to the database configuration");
            PluginsManager.getInstance().getPluginDatabaseEntities().forEach(config::addAnnotatedClass);

            // Adding the plugin ClassLoader to the Hibernate configuration
            StandardServiceRegistry serviceRegistry =
                    new StandardServiceRegistryBuilder(
                            new BootstrapServiceRegistryBuilder()
                                    .applyClassLoader(PluginLoader.getInstance().getPluginsClassLoader())
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

            updateMessage("Updating database");
            updateProgress(0, 1);
            executeSubtask(new DatabaseUpdateTask(() ->
            {
                try
                {
                    return databaseConfigData.getConnection();
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }));
            updateProgress(1, 1);
            return null;
        }
    }

    @AllArgsConstructor
    private static class DatabaseUpdateTask extends Task<Void>
    {
        private final Supplier<Connection> connFactory;

        @Override
        protected Void call() throws Exception
        {
            updateTitle("Updating database structure");
            updateMessage("Reading loaded plugins");
            updateProgress(0, 1);

            // Create the database version table if it doesn't exist
            try (Connection conn = connFactory.get())
            {
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
            }

            updateMessage("Ask each plugin to update");

            // Update the database version for each plugin
            var plugins = PluginsManager.getInstance().getLoadedPlugins();
            int i = 0;
            for (var plugin : plugins)
            {
                updateDatabaseFor(connFactory, plugin.getPluginData().pluginId, plugin);
                i++;
                updateProgress(i, plugins.size());
            }

            return null;
        }

        private void updateDatabaseFor(Supplier<Connection> connFactory, String identifier, IDatabaseUpdater updater) throws Exception
        {
            var newVersion = updater.getDatabaseVersion();

            int oldVersion = 0;
            boolean exists = false;
            
            try (Connection conn = connFactory.get())
            {
                String sql = """
                    select version from core_database_version where plugin_identifier = ?
                    """;

                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, identifier);

                var result = statement.executeQuery();

                if (result.next())
                {
                    oldVersion = result.getInt(1);
                    exists = true;
                }
            }

            if (oldVersion == newVersion) return;

            Dbms dbms = new DatabaseConfigData().load().getDbms();

            try (Connection conn = connFactory.get())
            {
                conn.createStatement().execute(
                        switch (dbms)
                        {
                            case MARIADB -> "set foreign_key_checks = 0";
                            case SQLITE -> "PRAGMA foreign_keys = OFF";
                            default -> "select 1";
                        }
                );
            }

            String formattedSql = exists
                    ? "update core_database_version set version = %d where plugin_identifier = '%s'"
                    : "insert into core_database_version (version, plugin_identifier) values (%d, '%s')";

            if (oldVersion < newVersion)
            {
                for (int i = oldVersion + 1; i <= newVersion; i++)
                {
                    Connection conn = connFactory.get();
                    conn.setAutoCommit(false);
                    
                    try
                    {
                        updater.upgradeDatabaseTo(conn, i, dbms);
                        conn.createStatement().executeUpdate(String.format(formattedSql, i, identifier));
                        conn.commit();
                    }
                    catch (SQLException ex)
                    {
                        Logs.getInstance().log(
                                "Error upgrading database to version " + i + " for " + identifier,
                                ex
                        );
                        conn.rollback();
                        throw new Exception("Error updating the database");
                    }
                    finally
                    {
                        conn.close();
                    }
                }
            }
            else
            {
                for (int i = oldVersion - 1; i >= newVersion; i--)
                {
                    Connection conn = connFactory.get();
                    conn.setAutoCommit(false);
                    
                    try
                    {
                        updater.downgradeDatabaseTo(conn, i, dbms);
                        conn.createStatement().executeUpdate(String.format(formattedSql, i, identifier));
                        conn.commit();
                    }
                    catch (SQLException ex)
                    {
                        Logs.getInstance().log(
                                "Error downgrading database to version " + i + " for " + identifier,
                                ex
                        );
                        conn.rollback();
                        throw new Exception("Error downgrading the database");
                    }
                    finally
                    {
                        conn.close();
                    }
                }
            }

            // This is executted even if the update fails
            try (Connection conn = connFactory.get())
            {
                conn.createStatement().execute(
                        switch (dbms)
                        {
                            case MARIADB -> "set foreign_key_checks = 1";
                            case SQLITE -> "PRAGMA foreign_keys = ON";
                            default -> "select 1";
                        }
                );
            }
        }
    }
}
