package org.lebastudios.theroundtable.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class DesktopAppDatabaseUpdater implements IDatabaseUpdater
{
    private static final int DB_VERSION = 1;

    @Override
    public int getDatabaseVersion()
    {
        return DB_VERSION;
    }

    public void upgradeTo1(Dbms dbms, Connection conn) throws SQLException, IOException
    {
        System.out.println("Desktop App: Updating database to version 1");

        Statement statement = conn.createStatement();

        statement.addBatch("""
                create table core_account
                (
                    id                             integer,
                    changue_password_on_next_login boolean      not null,
                    name                           varchar(255) not null,
                    password                       varchar(255) not null,
                    type                           varchar(255) not null,
                    constraint ck_account_type check (type in ('ROOT', 'ADMIN', 'MANAGER', 'CASHIER', 'ACCOUNTANT')),
                    constraint pk_account primary key (id)
                );""");

        statement.executeBatch();
    }

    public void downgradeTo0(Dbms dbms, Connection conn) throws SQLException, IOException
    {
        System.out.println("Desktop App: Updating database to version 1");

        Statement statement = conn.createStatement();

        statement.addBatch("drop table core_account;");

        statement.executeBatch();
    }
}
