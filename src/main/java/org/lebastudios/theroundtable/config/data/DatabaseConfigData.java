package org.lebastudios.theroundtable.config.data;

import org.hibernate.cfg.Configuration;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.config.Settings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfigData implements FileRepresentator
{
    public enum Dbms {
        MYSQL, MARIADB, POSTGRES;

        public String getJdbcIdentifier()
        {
            return switch (this) {
                case MYSQL -> "mysql";
                case MARIADB -> "mariadb";
                case POSTGRES -> "postgresql";
            };
        }
        
        public String getJdbcDriver()
        {
            return switch (this) {
                case MYSQL -> "com.mysql.cj.jdbc.Driver";
                case MARIADB -> "org.mariadb.jdbc.Driver";
                case POSTGRES -> "org.postgresql.Driver";
            };
        }
        
        public String getHibernateDialect()
        {
            return switch (this) {
                case MYSQL -> "org.hibernate.dialect.MySQLDialect";
                case MARIADB -> "org.hibernate.dialect.MariaDBDialect";
                case POSTGRES -> "org.hibernate.dialect.PostgreSQLDialect";
            };
        }
        
        @Override
        public String toString()
        {
            return switch (this) {
                case MYSQL -> "MySQL";
                case MARIADB -> "MariaDB";
                case POSTGRES -> "PostgresSQL";
            };
        }
    }
    
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

    public String getJdbcDriver()
    {
        return enableRemoteDb 
                ? remoteDbData.dbms.getJdbcDriver()
                : "org.sqlite.JDBC";
    }

    public String getHibernateDialect()
    {
        return enableRemoteDb
                ? remoteDbData.dbms.getHibernateDialect()
                : "org.hibernate.community.dialect.SQLiteDialect";
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
        return new File(Settings.getGlobalDir() + "/database.json");
    }
}
