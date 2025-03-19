package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.config.DatabaseConfigData;

import java.sql.Connection;
import java.util.Arrays;

public interface IDatabaseUpdater
{
    default int getDatabaseVersion() { return 0; }
    default void updateDatabase(Connection conn, int oldVersion, int newVersion) throws Exception 
    {
        Dbms dbms = new DatabaseConfigData().load().getDbms();
        
        for (int i = oldVersion - 1; i >= newVersion; i--)
        {
            var methodName = "downgradeTo" + i;
            var updateMethod = Arrays.stream(this.getClass().getMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .findFirst();

            if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

            updateMethod.get().invoke(this, conn, dbms);
        }
        
        for (int i = oldVersion + 1; i <= newVersion; i++)
        {
            var methodName = "upgradeTo" + i;
            var updateMethod = Arrays.stream(this.getClass().getMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .findFirst();

            if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

            updateMethod.get().invoke(this, conn, dbms);
        }
    }
}
