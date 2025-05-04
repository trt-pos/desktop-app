package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.env.Variables;
import org.lebastudios.theroundtable.server.Server;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginsConfigData extends ConfigData<PluginsConfigData>
{
    public String pluginsFolder = TheRoundTableApplication.getUserDirectory() + "/plugins/";
    public Set<String> repos = new HashSet<>(
            List.of(Server.CENTRAL_PLUGIN_REPO_BASE_URL)
    );

    public PluginsConfigData() 
    {
        if (Variables.isDev())
        {
            repos.add(
                    "http://localhost:9503"
            );
        }
    }
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir() + "/plugins.json");
    }
}
