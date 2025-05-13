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

        getInstance().resourceBundles.put(plugin.getPackageName() + ".lang", resourceBundle);
    }
    
    public String t(String key)
    {
        for (var resourceBundle : resourceBundles.values())
        {
            try
            {
                return resourceBundle.getString(key);
            }
            catch (MissingResourceException _) {}
        }

        Logs.getInstance().log(
                Logs.LogType.WARNING,
                "Key not found: " + key
        );

        return key;
    }
    
    public String t(String key, Class<? extends IPlugin> plugin)
    {
        try
        {
            return resourceBundles.get(plugin.getPackageName() + ".lang").getString(key);
        }
        catch (MissingResourceException _) 
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Key not found: " + key
            );
            
            return key;
        }
    }
}
