package org.lebastudios.theroundtable.plugins;

import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.config.SettingsItem;
import org.lebastudios.theroundtable.ui.LabeledIconButton;

import java.util.*;

@Getter
public class PluginsManager
{
    private static PluginsManager instance;
    
    public static PluginsManager getInstance()
    {
        if (instance == null) instance = new PluginsManager();
        
        return instance;
    }

    private final Map<String, IPlugin> pluginsLoaded = new HashMap<>();
    private final Map<String, IPlugin> pluginsInstalled = new HashMap<>();
    private final Map<String, PluginData> pluginsRestartPending = new HashMap<>();
    
    private PluginsManager() {}

    public List<Button> getLeftButtons()
    {
        List<Button> buttons = new ArrayList<>();
        for (IPlugin plugin : pluginsLoaded.values())
        {
            buttons.addAll(plugin.getLeftButtons());
        }
        return buttons;
    }

    public List<Button> getRightButtons()
    {
        List<Button> buttons = new ArrayList<>();
        for (IPlugin plugin : pluginsLoaded.values())
        {
            buttons.addAll(plugin.getRightButtons());
        }
        return buttons;
    }

    public List<LabeledIconButton> getHomeButtons()
    {
        List<LabeledIconButton> buttons = new ArrayList<>();
        for (IPlugin plugin : pluginsLoaded.values())
        {
            buttons.addAll(plugin.getHomeButtons());
        }
        return buttons;
    }

    public List<TreeItem<SettingsItem>> getSettingsTreeViews()
    {
        List<TreeItem<SettingsItem>> items = new ArrayList<>();

        for (IPlugin plugin : pluginsLoaded.values())
        {
            var rootTreeItem = plugin.getSettingsRootTreeItem();
            if (rootTreeItem == null) continue;

            items.add(rootTreeItem);
        }

        return items;
    }

    public List<Object> getRessourcesObjects()
    {
        List<Object> classes = new ArrayList<>();

        classes.add(new TheRoundTableApplication());
        classes.addAll(getLoadedPlugins());

        return classes;
    }

    public List<Class<?>> getPluginDatabaseEntities()
    {
        List<Class<?>> pluginEntities = new ArrayList<>();
        for (IPlugin plugin : pluginsLoaded.values())
        {
            pluginEntities.addAll(plugin.getPluginEntities());
        }
        return pluginEntities;
    }

    ///  Installed, loaded in memory and in execution plugins
    public Collection<IPlugin> getLoadedPlugins()
    {
        return pluginsLoaded.values();
    }

    ///  Installed, loaded in memory but not in execution plugins
    public Collection<IPlugin> getInstalledPlugins()
    {
        return pluginsInstalled.values();
    }

    ///  Installed but not yet loaded in memory plugins
    public Collection<PluginData> restartPendingPlugins()
    {
        return pluginsRestartPending.values();
    }

    public void uninstallPlugin(PluginData pluginData)
    {
        pluginsInstalled.remove(pluginData.pluginId);
    }

    public boolean isPluginInstalled(PluginData pluginData)
    {
        return pluginsInstalled.containsKey(pluginData.pluginId);
    }

    public boolean isPluginLoaded(PluginData pluginData)
    {
        return pluginsLoaded.containsKey(pluginData.pluginId);
    }
}
