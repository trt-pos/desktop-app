package org.lebastudios.theroundtable.config;

import org.hibernate.cfg.Configuration;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.database.Dbms;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfigData extends ConfigData<DatabaseConfigData>
{
    public String establishmentDatabaseName = "establishment";
    
    public String databaseFolder = TheRoundTableApplication.getUserDirectory() + File.separator + "databases";
    public boolean enableBackups = false;
    public int numMaxBackups = 5;
    public String backupFolder = TheRoundTableApplication.getUserDirectory() + File.separator + "trt-db-backups";
    
    public boolean enableRemoteDb = false;
    public RemoteDbData remoteDbData;

    public static class RemoteDbData
    {

        public Dbms dbms = Dbms.MARIADB;
        public String host;
        public String port;
        public String user;
        public String password;
        public String database;
        public Connection getConnection() throws SQLException
        {
            return DriverManager.getConnection(
                    getJdbcUrl(),
                    user,
                    password
            );
        }

        public String getJdbcUrl()
        {
            return "jdbc:" + dbms.getJdbcIdentifier() + "://" + host + ":" + port + "/" + database;
        }

    }
    public Configuration getHibernateConf()
    {
        Configuration config = new Configuration().configure();

        config.setProperty(
                "hibernate.connection.driver_class", getJdbcDriver()
        );
        config.setProperty(
                "hibernate.dialect", getHibernateDialect()
        );
        config.setProperty(
                "hibernate.connection.url", getJdbcUrl()
        );
        
        if (enableRemoteDb) 
        {
            config.setProperty(
                    "hibernate.connection.username", remoteDbData.user
            );
            config.setProperty(
                    "hibernate.connection.password", remoteDbData.password
            );
        }
        
        return config;
    }

    public Connection getConnection() throws SQLException
    {
        return enableRemoteDb 
                ? remoteDbData.getConnection() 
                : DriverManager.getConnection("jdbc:sqlite:" + getSQLiteDatabaseFile().getAbsolutePath()
        );
    }

    public Dbms getDbms()
    {
        return enableRemoteDb
                ? remoteDbData.dbms
                : Dbms.SQLITE;
    }

    public String getJdbcDriver()
    {
        return getDbms().getJdbcDriver();
    }

    public String getHibernateDialect()
    {
        return getDbms().getHibernateDialect();
    }
    
    public String getJdbcUrl()
    {
        return enableRemoteDb 
                ? remoteDbData.getJdbcUrl()
                : "jdbc:sqlite:" + getSQLiteDatabaseFile().getAbsolutePath();
    }
    
    public File getSQLiteDatabaseFile()
    {
        new File(databaseFolder).mkdirs();
        
        return new File(databaseFolder, establishmentDatabaseName + ".sqlite");
    }
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir() + "/database.json");
    }
}
