package org.lebastudios.theroundtable.apparience;

import javafx.scene.Scene;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.files.JsonFile;
import org.lebastudios.theroundtable.config.PreferencesConfigData;
import org.lebastudios.theroundtable.events.UserEvents;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ThemeLoader
{
    private static final Set<Scene> scenesInstantiated = new HashSet<>();

    static {
        UserEvents.OnAccountLogIn.addListener(a -> reloadThemes());
    }
    
    public synchronized static void reloadThemes()
    {
        for (var scene : scenesInstantiated)
        {
            scene.getStylesheets().removeLast();
            addActualTheme(scene);
        }
    }

    @SneakyThrows
    public static Scene addActualTheme(Scene scene)
    {
        removeRemovedScenes();

        scenesInstantiated.add(scene);
        
        var actualTheme = new PreferencesConfigData().load().theme;

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
