package org.lebastudios.theroundtable.plugins;

import lombok.AllArgsConstructor;
import org.lebastudios.theroundtable.server.Server;

@AllArgsConstructor
public class PluginRepoData
{
    public String url;
    
    public PluginRepoIntrospector intoIntrospector()
    {
        return new PluginRepoIntrospector(url);
    }
    
    public static PluginRepoData centralRepo()
    {
        return new PluginRepoData(Server.CENTRAL_PLUGIN_REPO_BASE_URL);
    }
}
