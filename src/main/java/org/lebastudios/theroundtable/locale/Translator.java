package org.lebastudios.theroundtable.locale;

import javafx.fxml.FXMLLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.util.*;

public class Translator
{
    private static Translator instance;
    public static Translator getInstance()
    {
        if (instance == null) instance = new Translator();
        
        return instance;
    }

    private final Map<String, ResourceBundle> resourceBundles = new HashMap<>();

    private Translator() {}
    
    private void reloadT()
    {
        PluginsManager.getInstance()
                .getLoadedPlugins()
                .forEach(plugin -> this.loadT(plugin.getClass(), LocaleManager.getInstance().getActualLocale()));
    }

    public void injectT(FXMLLoader loader, Class<? extends IPlugin> pluginClazz)
    {
        loader.setResources(getInstance().resourceBundles.get(pluginClazz.getPackageName() + ".lang"));
    }
    
    public void loadT(Class<? extends IPlugin> plugin, Locale locale)
    {
        ResourceBundle resourceBundle;

        try
        {
            resourceBundle = ResourceBundle.getBundle(
                    plugin.getPackageName() + ".lang",
                    locale,
                    plugin.getClassLoader()
            );
        }
        catch (MissingResourceException exception)
        {
            resourceBundle = ResourceBundle.getBundle(
                    plugin.getPackageName() + ".lang",
                    Locale.of("en", "US"),
                    plugin.getClassLoader()
            );
        }

        getInstance().resourceBundles.put(
                PluginsManager.getInstance().getPluginOf(plugin).orElseThrow().getPluginData().id,
                resourceBundle
        );
    }
    
    public String t(String fullKey)
    {
        int splitIndex = fullKey.indexOf(':');
        
        if (splitIndex == -1) 
        {
            throw new IllegalArgumentException("Missing plugin ID in fullKey: " + fullKey);
        }
        
        String pluginId = fullKey.substring(0, splitIndex);
        String key = fullKey.substring(splitIndex + 1);

        return t(pluginId, key);
    }
    
    public String t(Class<? extends IPlugin> plugin, String key)
    {
        String pluginId = PluginsManager.getInstance()
                .getPluginOf(plugin)
                .orElseThrow()
                .getPluginData()
                .id;
        
        return t(pluginId, key);
    }
    
    public String t(String pluginId, String key)
    {
        try
        {
            return resourceBundles.get(pluginId).getString(key);
        }
        catch (Exception _)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Key not found: " + pluginId + ":" + key
            );

            return key;
        }
    }
}
