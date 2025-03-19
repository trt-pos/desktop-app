package org.lebastudios.theroundtable.database;

public enum Dbms
{
    SQLITE, MYSQL, MARIADB, POSTGRES;

    public String getJdbcIdentifier()
    {
        return switch (this)
        {
            case SQLITE -> "sqlite";
            case MYSQL -> "mysql";
            case MARIADB -> "mariadb";
            case POSTGRES -> "postgresql";
        };
    }

    public String getJdbcDriver()
    {
        return switch (this)
        {
            case SQLITE -> "org.sqlite.JDBC";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case POSTGRES -> "org.postgresql.Driver";
        };
    }

    public String getHibernateDialect()
    {
        return switch (this)
        {
            case SQLITE -> "org.hibernate.community.dialect.SQLiteDialect";
            case MYSQL -> "org.hibernate.dialect.MySQLDialect";
            case MARIADB -> "org.hibernate.dialect.MariaDBDialect";
            case POSTGRES -> "org.hibernate.dialect.PostgreSQLDialect";
        };
    }

    @Override
    public String toString()
    {
        return switch (this)
        {
            case SQLITE -> "SQLite";
            case MYSQL -> "MySQL";
            case MARIADB -> "MariaDB";
            case POSTGRES -> "PostgresSQL";
        };
    }
}
