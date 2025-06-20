package org.lebastudios.theroundtable.plugins;

import lombok.Getter;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.locale.LocaleManager;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class PluginLoader
{
    private static PluginLoader instance;

    public static PluginLoader getInstance()
    {
        if (instance == null) instance = new PluginLoader();

        return instance;
    }

    private URLClassLoader pluginsClassLoader = new URLClassLoader(new URL[0]);

    private PluginLoader() {}

    public Task<Void> loadPluginsTask()
    {
        return new LoadPluginsTask();
    }

    private class LoadPluginsTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Loading plugins");

            updateMessage("Validating installed plugins");
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

                    if (pluginData.requiredCoreVersion() == null)
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Plugin " + pluginData.name +
                                        " does not specify a required core version so it will be ignored"
                        );
                        continue;
                    }

                    pluginsManager.getPluginsInstalled().put(pluginData.id, plugin);
                }

            }
            catch (Throwable e)
            {
                Logs.getInstance().log(
                        "Error loading some plugins after checking that them can be loaded, something is wrong",
                        e
                );
            }

            updateMessage("Loading validated plugins");
            boolean keepTryingToLoad = true;

            while (keepTryingToLoad)
            {
                keepTryingToLoad = false;
                for (IPlugin plugin : pluginsManager.getPluginsInstalled().values())
                {
                    var pluginData = plugin.getPluginData();

                    if (pluginsManager.getPluginsLoaded().containsKey(pluginData.id)) continue;
                    if (!plugin.getPluginData().areDependenciesInstalled()) continue;

                    keepTryingToLoad = true;

                    updateMessage("Initializing plugin " + pluginData.name);

                    // Add plugin to the loaded plugins collection
                    pluginsManager.getPluginsLoaded().put(plugin.getPluginData().id, plugin);
                    
                    // All the chewcks passed, the plugin can be considered load and the user will be able to use it
                    // Load plugin translations
                    Translator.getInstance().loadT(plugin.getClass(), LocaleManager.getInstance().getActualLocale());
                }
            }

            return null;
        }

        private List<URL> getValidJars()
        {
            List<File> pluginsFound = getInstalledPluginsJars();

            Set<String> repeatedPluginsIds = new HashSet<>();
            Map<String, URL> validJarsMap = new HashMap<>();

            for (File jar : pluginsFound)
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
                    IPlugin plugin = ServiceLoader.load(IPlugin.class, tempClassLoader).iterator().next();
                    String pluginId = plugin.getPluginData().id;
                    
                    if (repeatedPluginsIds.contains(pluginId))
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Plugin id " + pluginId + " has been found in multiple plugins: " + jarURL
                                
                        );
                        continue;
                    }
                    
                    if (validJarsMap.containsKey(pluginId))
                    {
                        repeatedPluginsIds.add(pluginId);
                        validJarsMap.remove(pluginId);
                        
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Plugin id " + pluginId + " has been found in multiple plugins: " + jarURL
                        );
                        continue;
                    }
                    
                    validJarsMap.put(pluginId, jarURL);
                }
                catch (Throwable e)
                {
                    Logs.getInstance().log("Error loading plugin " + jar.getName() + " while validating it", e);
                }
            }

            return new ArrayList<>(validJarsMap.values());
        }

        private List<File> getInstalledPluginsJars()
        {
            List<File> foundJars = new ArrayList<>();
            File pluginsFolder = new File(new PluginsConfigData().load().pluginsFolder);
            
            if (!pluginsFolder.exists())
            {
                return foundJars;
            }

            final var files = pluginsFolder.listFiles();
            
            if (files == null)
            {
                Logs.getInstance().log(
                        Logs.LogType.ERROR,
                        "Error reading plugins folder: " + pluginsFolder.getAbsolutePath()
                );
                return foundJars;
            }
            
            for (File file : files)
            {
                if (!file.isDirectory()) continue;

                final var plugins = file.listFiles((_, name) -> name.endsWith(".jar"));
                
                if (plugins == null || plugins.length == 0)
                {
                    Logs.getInstance().log(
                            Logs.LogType.WARNING,
                            "Error reading plugins in directory: " + file.getAbsolutePath()
                    );
                    continue;
                }
                
                foundJars.addAll(Arrays.asList(plugins));
            }
            
            return foundJars;
        }
    }
}
