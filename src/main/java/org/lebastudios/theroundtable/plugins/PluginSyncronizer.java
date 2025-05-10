package org.lebastudios.theroundtable.plugins;

import org.hibernate.Session;
import org.lebastudios.theroundtable.communications.FileTransferServiceManager;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.database.entities.Plugin;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PluginSyncronizer
{
    public void syncWithMaster() throws Exception
    {
        AtomicReference<Exception> exception = new AtomicReference<>();

        Database.getInstance().connectQuery(session ->
        {
            try
            {
                syncWithMaster(session);
            }
            catch (IOException e)
            {
                exception.set(e);
            }
        });
        
        if (exception.get() != null)
        {
            throw exception.get();
        }
    }
    
    public void syncWithMaster(Session session) throws IOException
    {
        List<org.lebastudios.theroundtable.database.entities.Plugin>
                plugins = session.createQuery("from Plugin", org.lebastudios.theroundtable.database.entities.Plugin.class)
                .getResultList();

        for (Plugin plugin : plugins)
        {
            File pluginFile = new File(
                    new File(
                            new PluginsConfigData().load().pluginsFolder,
                            plugin.getRepo().getHost() + ":" + plugin.getRepo().getPort()
                    ),
                    plugin.getId() + ".jar"
            );

            if (!pluginFile.exists())
            {
                // This installation doesn't have the plugin, so we don't want to
                // update it from the master.
                // That would be instrusive
                continue;
            }

            try (BufferedInputStream reader = new BufferedInputStream(
                    FileTransferServiceManager.getInstance()
                            .getNetFileInputStream(pluginFile.getAbsolutePath(), session));
                 BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(pluginFile)))
            {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1)
                {
                    writer.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
