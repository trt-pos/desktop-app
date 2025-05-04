package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.env.Variables;
import org.lebastudios.theroundtable.server.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginsConfigData extends ConfigData<PluginsConfigData>
{
    public String pluginsFolder = TheRoundTableApplication.getUserDirectory() + "/plugins/";
    public String centralRepo = Server.CENTRAL_PLUGIN_REPO_BASE_URL;
    public Set<String> customRepos = new HashSet<>();

    public PluginsConfigData() 
    {
        if (Variables.isDev())
        {
            customRepos.add(
                    "http://localhost:9503"
            );
        }
    }
    
    public List<String> getAllRepos()
    {
        ArrayList<String> allRepos = new ArrayList<>();
        
        allRepos.add(centralRepo);
        allRepos.addAll(customRepos);
        
        return allRepos;
    }
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir() + "/plugins.json");
    }
}
