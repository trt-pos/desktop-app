package org.lebastudios.theroundtable.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.lebastudios.theroundtable.config.data.DatabaseConfigData;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.database.entities.Account;
import org.lebastudios.theroundtable.database.entities.DatabaseVersion;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.events.DatabaseEvents;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;

import java.sql.Connection;
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

    public void init()
    {
        if (getInstance().sessionFactory != null) return;

        getInstance().sessionFactory = buildSessionFactory();
        DatabaseEvents.onDatabaseInit.invoke();

        AppLifeCicleEvents.OnAppClose.addListener((_) ->
        {
            DatabaseEvents.onDatabaseClose.invoke();
            getInstance().sessionFactory.close();
        });
    }
    
    public void reload()
    {
        DatabaseEvents.onDatabaseClose.invoke();
        sessionFactory.close();

        sessionFactory = buildSessionFactory();
        DatabaseEvents.onDatabaseInit.invoke();
    }
    
    private SessionFactory buildSessionFactory()
    {
        DatabaseConfigData databaseConfigData = new JSONFile<>(DatabaseConfigData.class).get();

        if (!prepareDatabaseForHibernate(databaseConfigData)) return null;

        try
        {
            var config = databaseConfigData.getHibernateConf();

            config.addAnnotatedClass(Account.class)
                    .addAnnotatedClass(DatabaseVersion.class);

            // Loading all the plugin entities to the Hibernate configuration from the Plugins
            PluginLoader.getPluginEntities().forEach(config::addAnnotatedClass);

            // Adding the plugin ClassLoader to the Hibernate configuration
            StandardServiceRegistry serviceRegistry =
                    new StandardServiceRegistryBuilder(
                            new BootstrapServiceRegistryBuilder().applyClassLoader(PluginLoader.getPluginsClassLoader())
                                    .build())
                            .applySettings(config.getProperties())
                            .build();

            return config.buildSessionFactory(serviceRegistry);
        }
        catch (Exception ex)
        {
            Logs.getInstance().log("SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private boolean prepareDatabaseForHibernate(DatabaseConfigData databaseConfigData)
    {
        try (Connection conn = databaseConfigData.getConnection())
        {
            DatabaseUpdater.getInstance().callToUpdate(conn);
            return true;
        }
        catch (SQLException e)
        {
            Logs.getInstance().log("Database update failed.", e);
            return false;
        }
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
}
