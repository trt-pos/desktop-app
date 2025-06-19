package org.lebastudios.theroundtable.plugins;

import com.google.gson.annotations.SerializedName;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginData
{
    public String name;
    public String id;
    public String descriprion;
    public String version;
    public String vendor;
    @SerializedName(value = "vendor-url")
    public String vendorUrl;
    public String[] tags;
    public String[] category;
    public PluginDependencyData[] dependencies;
    
    public String getDbTablePrefix()
    {
        return id.replace(".", "_").replace("-", "_");
    }
    
    public boolean areDependenciesInstalled()
    {
        for (var pluginDependencyNeeded : this.dependencies)
        {
            List<PluginData> pluginsData = PluginsManager.getInstance().getInstalledPlugins().stream()
                    .map(IPlugin::getPluginData)
                    .collect(Collectors.toList());
            pluginsData.addAll(PluginsManager.getInstance().restartPendingPlugins());

            var pluginDependencyFound = pluginsData.stream()
                    .filter(p -> p.id.equals(pluginDependencyNeeded.pluginId))
                    .findFirst()
                    .orElse(null);

            if (pluginDependencyFound == null)
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + this.name
                        + " requires the plugin " + pluginDependencyNeeded.pluginId + " to be installed."
                );
                return false;
            }
            
            final var installedDependencyVersion = new Version(pluginDependencyFound.version);
            final var neededDependencyVersion = new Version(pluginDependencyNeeded.pluginVersion);

            if (!installedDependencyVersion.hasSameMajor(neededDependencyVersion))
            {
                Logs.getInstance().log(
                        Logs.LogType.INFO,
                        "The plugin " + this.name
                                + " requires a different major version of the plugin " + pluginDependencyNeeded.pluginId
                );
                return false;
            }

            if (installedDependencyVersion.isLessThan(neededDependencyVersion))
            {
                Logs.getInstance().log(Logs.LogType.INFO, "The plugin " + this.name
                        + " requires the plugin " + pluginDependencyNeeded.pluginId + " to be updated."
                );
                return false;
            }
        }

        return true;
    }

    public String requiredCoreVersion()
    {
        return Arrays.stream(dependencies)
                .filter(data -> data.pluginId.equals(CorePlugin.getInstance().getPluginData().id))
                .map(data -> data.pluginVersion)
                .findFirst()
                .orElse(null);
    }
    
    public boolean isDependencyOfOther()
    {
        for (IPlugin other : PluginsManager.getInstance().getPluginsInstalled().values())
        {
            PluginData otherPluginData = other.getPluginData();

            if (Arrays.stream(otherPluginData.dependencies)
                    .anyMatch(data -> data.pluginId.equals(this.id)))
            {
                return true;
            }
        }

        for (PluginData otherPluginData : PluginsManager.getInstance().getPluginsRestartPending().values())
        {
            if (Arrays.stream(otherPluginData.dependencies)
                    .anyMatch(data -> data.pluginId.equals(this.id)))
            {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PluginData that)) return false;

        return Objects.equals(id, that.id);
    }
}
