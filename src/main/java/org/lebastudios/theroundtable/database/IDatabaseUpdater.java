package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.config.DatabaseConfigData;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;

public interface IDatabaseUpdater
{
    default int getDatabaseVersion() { return 0; }

    default void updateDatabase(Connection conn, int oldVersion, int newVersion) throws Exception 
    {
        if (oldVersion == newVersion) return;
        
        Class<? extends IPlugin> clazz = PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();
        
        if (new MethodUpdateStrategy().updateDatabase(conn, oldVersion, newVersion, clazz)) return;
        if (new SQLFileUpdateStrategy().updateDatabase(conn, oldVersion, newVersion, clazz)) return;
        
        throw new Exception("No update strategy found when updating from version " + oldVersion + " to " + newVersion);
    }
    
    interface IUpdateStrategy
    {
        boolean updateDatabase(Connection conn, int oldVersion, int newVersion, Class<? extends IPlugin> clazz) throws Exception;
    }
    
    class MethodUpdateStrategy implements IUpdateStrategy
    {
        @Override
        public boolean updateDatabase(Connection conn, int oldVersion, int newVersion, Class<? extends IPlugin> clazz) throws Exception
        {
            IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();
            Method[] methods = clazz.getDeclaredMethods();
            Optional<Method> someMethod = Arrays.stream(methods)
                    .filter(method -> method.getName().startsWith("downgradeTo") || method.getName().startsWith("upgradeTo"))
                    .findFirst();

            if (someMethod.isEmpty()) return false;

            Dbms dbms = new DatabaseConfigData().load().getDbms();
            
            for (int i = oldVersion - 1; i >= newVersion; i--)
            {
                var methodName = "downgradeTo" + i;
                var updateMethod = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst();

                if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

                updateMethod.get().invoke(plugin, conn, dbms);
            }

            for (int i = oldVersion + 1; i <= newVersion; i++)
            {
                var methodName = "upgradeTo" + i;
                var updateMethod = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst();

                if (updateMethod.isEmpty()) throw new NoSuchMethodException(methodName);

                updateMethod.get().invoke(plugin, conn, dbms);
            }
            
            return true;
        }
    }
    
    class SQLFileUpdateStrategy implements IUpdateStrategy
    {
        private static final String DELIMITER = "-- DELIMITER";
        
        private enum UpdateType
        {
            UPGRADE,
            DOWNGRADE
        }
        
        @Override
        public boolean updateDatabase(Connection conn, int oldVersion, int newVersion, Class<? extends IPlugin> clazz) throws Exception
        {
            URL upgradeURL = clazz.getResource("sql/" + UpdateType.UPGRADE.name().toLowerCase());
            URL downgradeURL = clazz.getResource("sql/" + UpdateType.DOWNGRADE.name().toLowerCase());
            
            if (upgradeURL == null || downgradeURL == null) return false;

            File[] upgradeFiles = new File(upgradeURL.getFile()).listFiles(File::isFile);
            File[] downgradeFiles = new File(downgradeURL.getFile()).listFiles(File::isFile);
            
            if (upgradeFiles == null || downgradeFiles == null) return false;
            
            if (Arrays.stream(upgradeFiles).findFirst().isEmpty()
                && Arrays.stream(downgradeFiles).findFirst().isEmpty())
            {
                return false;
            }
            
            Dbms dbms = new DatabaseConfigData().load().getDbms();

            for (int i = oldVersion - 1; i >= newVersion; i--)
            {
                try (InputStream is = getSQLFile(dbms, UpdateType.DOWNGRADE, i, clazz))
                {
                    executeSQL(conn, is);
                }
            }

            for (int i = oldVersion + 1; i <= newVersion; i++)
            {
                try (InputStream is = getSQLFile(dbms, UpdateType.UPGRADE, i, clazz))
                {
                    executeSQL(conn, is);
                }
            }

            return true;
        }
        
        private InputStream getSQLFile(Dbms dbms, UpdateType updateType, int version, Class<? extends IPlugin> clazz) throws Exception
        {
            // Finding for a generic SQL file first
            String fileName = "sql/" + updateType.name().toLowerCase() + "/to-" + version + ".sql";
            InputStream is = clazz.getResourceAsStream(fileName);
            
            if (is != null) return is;
            
            // If not found, try to find a DBMS specific SQL file
            fileName = "sql/" + updateType.name().toLowerCase() + "/" + dbms.name().toLowerCase() + "-to-" + version + ".sql";
            is = clazz.getResourceAsStream(fileName);
            
            if (is != null) return is;
            
            throw new FileNotFoundException("Update sql file into version " + version + " not found.\n" 
                    + "The should be a file named as one of this options:\n" 
                    + "\tsql/<upgrade|downgrade>/to-<version>.sql\n"
                    + "\tsql/<upgrade|downgrade>/<dbms>-to-<version>.sql ");
        }
        
        private void executeSQL(Connection conn, InputStream is) throws SQLException, IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Statement statement = conn.createStatement();
            
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isBlank()) continue;
                
                if (line.trim().startsWith(DELIMITER))
                {
                    String sqlString = sql.toString();
                    
                    if (!sqlString.trim().isBlank()) statement.execute(sqlString);
                    
                    sql.setLength(0);
                    continue;
                }
                
                sql.append(line).append("\n");
            }

            String sqlString = sql.toString();

            if (!sqlString.trim().isBlank()) statement.execute(sqlString);
        }
    }
}
