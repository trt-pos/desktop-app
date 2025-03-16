package org.lebastudios.theroundtable.database;

import org.hibernate.Session;
import org.lebastudios.theroundtable.tasks.Task;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database
{
    private static Database instance;

    private Database() {}

    public static Database getInstance()
    {
        if (instance == null) instance = new Database();

        return instance;
    }

    public Task<Void> initTask()
    {
        return HibernateManager.getInstance().initTask();
    }

    public Task<Void> reloadTask()
    {
        return HibernateManager.getInstance().reloadTask();
    }

    public void initBackup()
    {
        BackupDB.getInstance().initialize();
    }

    public void stopBackup()
    {
        BackupDB.getInstance().stop();
    }
    
    public Exception migrateTables(Connection from, Connection to)
    {
        return new DatabaseMigrator(from, to).migrate();
    }
    
    public void connectTransaction(Consumer<Session> action)
    {
        HibernateManager.getInstance().connectTransaction(action);
    }
    
    public boolean connectTransactionWithBool(Consumer<Session> action)
    {
        return HibernateManager.getInstance().connectTransaction(action);
    }

    public void connectQuery(Consumer<Session> action)
    {
        HibernateManager.getInstance().connectQuery(session ->
        {
            action.accept(session);
            return null;
        });
    }

    public <R> R connectQuery(Function<Session, R> action)
    {
        return HibernateManager.getInstance().connectQuery(action);
    }
}
