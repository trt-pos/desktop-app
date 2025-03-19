package org.lebastudios.theroundtable.plugins;

import lombok.Getter;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.locale.AppLocale;
import org.lebastudios.theroundtable.locale.LangLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader
{
    @Getter private static URLClassLoader pluginsClassLoader = new URLClassLoader(new URL[0]);

    public static void loadPlugins()
    {
        pluginsClassLoader = new URLClassLoader(
                getValidJars().toArray(new URL[0]),
                PluginLoader.class.getClassLoader()
        );

        PluginsManager pluginsManager = PluginsManager.getInstance();
        
        try
        {
            ServiceLoader<IPlugin> serviceLoader = ServiceLoader.load(IPlugin.class, pluginsClassLoader);

            for (IPlugin plugin : serviceLoader)
            {
                var pluginData = plugin.getPluginData();
                pluginsManager.getPluginsInstalled().put(pluginData.pluginId, plugin);
            }

        }
        catch (Throwable e)
        {
            Logs.getInstance().log(
                    "Error loading some plugins after checking that them can be loaded, something is wrong",
                    e
            );
        }

        // Load all plugins that can be loaded
        boolean keepTryingToLoad = true;

        while (keepTryingToLoad)
        {
            keepTryingToLoad = false;
            for (IPlugin plugin : pluginsManager.getPluginsInstalled().values())
            {
                var pluginData = plugin.getPluginData();

                if (pluginsManager.getPluginsLoaded().containsKey(pluginData.pluginId)) continue;
                if (!plugin.getPluginData().areDependenciesInstalled()) continue;

                keepTryingToLoad = true;

                // All the chewcks passed, the plugin can be considered load and the user will be able to use it
                // Load plugin translations
                LangLoader.loadLang(plugin.getClass(), AppLocale.getActualLocale());

                // Initialize the plugin
                plugin.initialize();
                
                // Add plugin to the loaded plugins collection
                pluginsManager.getPluginsLoaded().put(plugin.getPluginData().pluginId, plugin);
            }
        }

        Database.getInstance().reloadTask().execute(true);
    }

    private static List<URL> getValidJars()
    {
        File[] jars = new File(new PluginsConfigData().load().pluginsFolder)
                .listFiles((_, name) -> name.endsWith(".jar"));

        if (jars == null) return new ArrayList<>();

        List<URL> validJars = new ArrayList<>();

        for (File jar : jars)
        {
            URL jarURL;

            try
            {
                jarURL = jar.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                Logs.getInstance().log(
                        "Error loading plugin " + jar.getName() + " (Malformed URL)",
                        e
                );
                continue;
            }

            try (URLClassLoader tempClassLoader = new URLClassLoader(
                    new URL[]{jarURL},
                    PluginLoader.class.getClassLoader()
            ))
            {
                ServiceLoader.load(IPlugin.class, tempClassLoader).iterator().next();
                validJars.add(jarURL);
            }
            catch (Throwable e)
            {
                Logs.getInstance().log("Error loading plugin " + jar.getName(), e);
            }
        }

        return validJars;
    }
}
