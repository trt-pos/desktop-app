package org.lebastudios.theroundtable.locale;

import javafx.fxml.FXMLLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.util.*;

public class LangBundleLoader
{
    private static LangBundleLoader instance;
    private final Map<String, ResourceBundle> resourceBundles = new HashMap<>();

    private LangBundleLoader() {}

    public static LangBundleLoader getInstance()
    {
        if (instance == null) instance = new LangBundleLoader();

        return instance;
    }

    public static void loadLang(Class<?> pluginClass, Locale locale)
    {
        ResourceBundle resourceBundle;

        try
        {
            resourceBundle = ResourceBundle.getBundle(
                    pluginClass.getPackageName() + ".lang",
                    locale,
                    pluginClass.getClassLoader()
            );
        }
        catch (MissingResourceException exception)
        {
            resourceBundle = ResourceBundle.getBundle(
                    pluginClass.getPackageName() + ".lang",
                    Locale.of("en", "US"),
                    pluginClass.getClassLoader()
            );
        }

        getInstance().resourceBundles.put(pluginClass.getPackageName() + ".lang", resourceBundle);
    }

    public static void loadLang(FXMLLoader loader, Class<? extends IPlugin> pluginClazz)
    {
        loader.setResources(getInstance().resourceBundles.get(pluginClazz.getPackageName() + ".lang"));
    }

    public String getString(String key)
    {
        for (var resourceBundle : resourceBundles.values())
        {
            try
            {
                return resourceBundle.getString(key);
            }
            catch (MissingResourceException _) {}
        }

        System.err.println("Key not found: " + key);

        return key;
    }
}
