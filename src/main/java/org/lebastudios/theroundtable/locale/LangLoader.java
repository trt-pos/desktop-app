package org.lebastudios.theroundtable.locale;

import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.util.Locale;

public class LangLoader
{
    static {
        AccountEvents.OnAccountLogIn.addListener(_ -> reloadLangs());
        AccountEvents.OnAccountLogOutAfter.addListener(LangLoader::reloadLangs);
    }
    
    private static void reloadLangs()
    {
        LangLoader.loadLang(Launcher.class, AppLocale.getActualLocale());

        PluginsManager.getInstance()
                .getLoadedPlugins()
                .forEach(plugin -> LangLoader.loadLang(plugin.getClass(), AppLocale.getActualLocale()));
    }
    
    public static void loadLang(Class<?> langClass, Locale locale)
    {
        Thread langFileThread = new Thread(() -> LangFileLoader.loadLang(locale, langClass));
        Thread langBundleThread = new Thread(() -> LangBundleLoader.loadLang(langClass, locale));
        
        langFileThread.start();
        langBundleThread.start();

        try
        {
            langFileThread.join();
            langBundleThread.join();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
