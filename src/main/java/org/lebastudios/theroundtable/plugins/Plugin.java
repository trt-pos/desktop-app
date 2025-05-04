package org.lebastudios.theroundtable.plugins;

import javafx.scene.image.Image;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.communications.Version;

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
}
