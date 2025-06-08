package org.lebastudios.theroundtable.plugins;

import lombok.Getter;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.locale.LocaleManager;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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
                                "Plugin " + pluginData.pluginName +
                                        " does not specify a required core version so it will be ignored"
                        );
                        continue;
                    }

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

            updateMessage("Loading validated plugins");
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

                    updateMessage("Initializing plugin " + pluginData.pluginName);

                    // Add plugin to the loaded plugins collection
                    pluginsManager.getPluginsLoaded().put(plugin.getPluginData().pluginId, plugin);
                    
                    // All the chewcks passed, the plugin can be considered load and the user will be able to use it
                    // Load plugin translations
                    Translator.getInstance().loadT(plugin.getClass(), LocaleManager.getInstance().getActualLocale());
                }
            }

            return null;
        }

        private List<URL> getValidJars()
        {
            movePluginsWithoutRepoToCentral();

            List<File> jars = new ArrayList<>();
            getInstalledPluginsJars(new File(new PluginsConfigData().load().pluginsFolder), jars);

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
                    Logs.getInstance().log("Error loading plugin " + jar.getName() + " while validating it", e);
                }
            }

            return validJars;
        }

        @SneakyThrows
        private void movePluginsWithoutRepoToCentral()
        {
            File[] jars = new File(new PluginsConfigData().load().pluginsFolder)
                    .listFiles((_, name) -> name.endsWith(".jar"));

            if (jars == null) return;
            if (jars.length == 0) return;

            File centralRepoFolder = new File(PluginRepoData.centralRepo().getLocalRepoFolder());
            if (centralRepoFolder.mkdirs())
            {
                PluginRepoData.centralRepo().save();
            }

            for (File jar : jars)
            {
                File finalJar = new File(centralRepoFolder, jar.getName());
                Files.move(jar.toPath(), finalJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        private void getInstalledPluginsJars(File folder, List<File> jars)
        {
            if (!folder.exists())
            {
                return;
            }
            
            for (File file : folder.listFiles())
            {
                if (file.isDirectory())
                {
                    getInstalledPluginsJars(file, jars);
                }
                else
                {
                    if (file.getName().equals("metadata.json"))
                    {
                        // Ignore the repo metadata file
                        continue;
                    }
                    
                    if (!file.getName().endsWith(".jar"))
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Plugin " + file.getName() + " does not specify a jar so it will be ignored"
                        );
                        continue;
                    }

                    jars.add(file);
                }
            }
        }
    }
}
