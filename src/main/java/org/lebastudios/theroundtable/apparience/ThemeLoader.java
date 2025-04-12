package org.lebastudios.theroundtable.apparience;

import javafx.scene.Scene;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.config.PreferencesConfigData;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThemeLoader
{
    private static final List<Scene> scenesInstantiated = new CopyOnWriteArrayList<>();
    private static boolean iterating = false;
    private static String actualTheme = new PreferencesConfigData().load().theme;
    
    static {
        AccountEvents.OnAccountLogIn.addListener(a -> reloadThemes());
    }
    
    public synchronized static void reloadThemes()
    {
        actualTheme = new PreferencesConfigData().load().theme;
        removeRemovedScenes();
        
        iterating = true;
        for (var scene : scenesInstantiated)
        {
            scene.getStylesheets().removeLast();
            addActualTheme(scene);
        }
        iterating = false;
    }

    @SneakyThrows
    public static Scene addActualTheme(Scene scene)
    {
        if (!iterating)
        {
            removeRemovedScenes();
            scenesInstantiated.add(scene);
        }

        String themeCss = new File(
                TheRoundTableApplication.getAppDirectory() + "/styles/" + actualTheme + "/theme.css")
                .toURI().toURL().toExternalForm();
        scene.getStylesheets().add(themeCss);
        return scene;
    }

    private static void removeRemovedScenes()
    {
        scenesInstantiated.removeIf(scene ->
        {
            final var window = scene.getWindow();
            
            if (window == null) 
            {
                Logs.getInstance().log(Logs.LogType.ERROR, "scene " + scene + " has no window");
                return false;
            }
            
            return !window.isShowing();
        });
    }

    public static File getThemesDir()
    {
        return new File(TheRoundTableApplication.getAppDirectory() + "/styles");
    }
}
