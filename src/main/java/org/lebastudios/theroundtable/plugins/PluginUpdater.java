package org.lebastudios.theroundtable.plugins;

import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.Version;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.pluginData.PluginData;

import java.util.List;
import java.util.stream.Collectors;

public class PluginUpdater
{
    public static boolean areDependenciesInstalled(PluginData pluginData)
    {
        if (pluginData == null) return false;

        if (new Version(pluginData.pluginRequiredCoreVersion)
                .compareTo(new Version(TheRoundTableApplication.getAppVersion())) > 0)
        {
            Logs.getInstance().log(
                    Logs.LogType.INFO, "The plugin " + pluginData.pluginName
                            + " requires a newer version of The Round Table."
            );
            return false;
        }

        for (var pluginDependencyNeeded : pluginData.pluginDependencies)
        {
            List<PluginData> pluginsData = PluginLoader.getInstalledPlugins().stream()
                    .map(IPlugin::getPluginData)
                    .collect(Collectors.toList());
            pluginsData.addAll(PluginLoader.restartPendingPlugins());

            var pluginDependencyFound = pluginsData.stream()
                    .filter(p -> p.pluginId.equals(pluginDependencyNeeded.pluginId))
                    .findFirst()
                    .orElse(null);

            if (pluginDependencyFound == null)
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + pluginData.pluginName
                        + " requires the plugin " + pluginDependencyNeeded.pluginId + " to be installed."
                );
                return false;
            }

            if (new Version(pluginDependencyFound.pluginVersion)
                    .compareTo(new Version(pluginDependencyNeeded.pluginVersion)) < 0)
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + pluginData.pluginName
                        + " requires the plugin " + pluginDependencyNeeded.pluginId + " to be updated."
                );
                return false;
            }
        }

        return true;
    }
}
