package org.lebastudios.theroundtable.plugins;

import javafx.scene.image.Image;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.config.PluginsConfigData;

import java.net.URI;

public record Plugin(PluginData data, PluginRepoData repoData, IPlugin plugin)
{
    @SneakyThrows
    public Image getPluginIcon()
    {
        return plugin == null
                ? ImageLoader.getWebImage(repoData.intoIntrospector().getWebIconUrl(
                data.pluginId, new Version(data.pluginVersion)))
                : plugin.getPluginIcon();
    }
    
    @SneakyThrows
    public String getLocalPath()
    {
        URI repoUri = new URI(repoData.url);
        return new PluginsConfigData().pluginsFolder 
                + repoUri.getHost() + "." + repoUri.getPort()
                + "/" + data.pluginId + ".jar";
    }
}
