package org.lebastudios.theroundtable.plugins;

import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.Version;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.List;
import java.util.stream.Collectors;

class PluginUpdater
{
    public static boolean hasDependenciesInstalled(PluginData pluginData)
    {
        if (pluginData == null) return false;

        final Version requiredCoreVersion = new Version(pluginData.pluginRequiredCoreVersion);
        final Version actualCoreVersion = new Version(TheRoundTableApplication.getAppVersion());

        if (!requiredCoreVersion.hasSameMajor(actualCoreVersion))
        {
            Logs.getInstance().log(
                    Logs.LogType.INFO,
                    "The plugin " + pluginData.pluginName
                            + " requires a different major version of The Round Table."
            );
            return false;
        }

        if (actualCoreVersion.isLessThan(requiredCoreVersion))
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


            final var installedDependencyVersion = new Version(pluginDependencyFound.pluginVersion);
            final var neededDependencyVersion = new Version(pluginDependencyNeeded.pluginVersion);

            if (!installedDependencyVersion.hasSameMajor(neededDependencyVersion))
            {
                Logs.getInstance().log(
                        Logs.LogType.INFO,
                        "The plugin " + pluginData.pluginName
                                + " requires a different major version of the plugin " + pluginDependencyNeeded.pluginId
                );
                return false;
            }

            if (installedDependencyVersion.isLessThan(neededDependencyVersion))
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
