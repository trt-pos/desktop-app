package org.lebastudios.theroundtable.plugins;

import lombok.AllArgsConstructor;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.files.JsonFile;
import org.lebastudios.theroundtable.server.Server;

import java.io.File;
import java.net.URI;

@AllArgsConstructor
public class PluginRepoData extends JsonFile<PluginRepoData>
{
    public String url;
    
    public PluginRepoIntrospector intoIntrospector()
    {
        return new PluginRepoIntrospector(url);
    }
    
    public String getLocalRepoFolder()
    {
        URI uri = URI.create(url);
        
        return new File(
                new PluginsConfigData().load().pluginsFolder,
                uri.getHost() + "." + uri.getPort()
        ).getAbsolutePath();
    }
    
    public static PluginRepoData centralRepo()
    {
        return new PluginRepoData(Server.CENTRAL_PLUGIN_REPO_BASE_URL);
    }

    @Override
    public File getFile()
    {
        return new File(getLocalRepoFolder(), "metadata.json");
    }
}
