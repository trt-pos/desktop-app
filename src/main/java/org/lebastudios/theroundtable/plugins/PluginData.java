package org.lebastudios.theroundtable.plugins;

import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.communications.Version;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginData
{
    public String pluginName;
    public String pluginId;
    public String pluginDescription;
    public String pluginVersion;
    public String pluginVendor;
    public String pluginVendorUrl;
    public String[] tags;
    public String category;
    public PluginDependencyData[] pluginDependencies;
    
    public boolean areDependenciesInstalled()
    {
        for (var pluginDependencyNeeded : this.pluginDependencies)
        {
            List<PluginData> pluginsData = PluginsManager.getInstance().getInstalledPlugins().stream()
                    .map(IPlugin::getPluginData)
                    .collect(Collectors.toList());
            pluginsData.addAll(PluginsManager.getInstance().restartPendingPlugins());

            var pluginDependencyFound = pluginsData.stream()
                    .filter(p -> p.pluginId.equals(pluginDependencyNeeded.pluginId))
                    .findFirst()
                    .orElse(null);

            if (pluginDependencyFound == null)
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + this.pluginName
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
                        "The plugin " + this.pluginName
                                + " requires a different major version of the plugin " + pluginDependencyNeeded.pluginId
                );
                return false;
            }

            if (installedDependencyVersion.isLessThan(neededDependencyVersion))
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + this.pluginName
                        + " requires the plugin " + pluginDependencyNeeded.pluginId + " to be updated."
                );
                return false;
            }
        }

        return true;
    }

    public String requiredDesktopAppVersion()
    {
        return Arrays.stream(pluginDependencies)
                .filter(data -> data.pluginId.equals(CorePlugin.getInstance().getPluginData().pluginId))
                .map(data -> data.pluginVersion)
                .findFirst()
                .orElse(null);
    }
    
    public boolean isDependencyOfOther()
    {
        for (IPlugin other : PluginsManager.getInstance().getPluginsInstalled().values())
        {
            PluginData otherPluginData = other.getPluginData();

            if (Arrays.stream(otherPluginData.pluginDependencies)
                    .anyMatch(data -> data.pluginId.equals(this.pluginId)))
            {
                return true;
            }
        }

        for (PluginData otherPluginData : PluginsManager.getInstance().getPluginsRestartPending().values())
        {
            if (Arrays.stream(otherPluginData.pluginDependencies)
                    .anyMatch(data -> data.pluginId.equals(this.pluginId)))
            {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(pluginId);
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PluginData that)) return false;

        return Objects.equals(pluginId, that.pluginId);
    }
}
