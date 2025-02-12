package org.lebastudios.theroundtable.apparience;

import javafx.scene.Scene;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PreferencesConfigData;
import org.lebastudios.theroundtable.events.UserEvents;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        
        var actualTheme = new JSONFile<>(PreferencesConfigData.class).get().theme;

        String themeCss = new File(
                TheRoundTableApplication.getAppDirectory() + "/styles/" + actualTheme + "/theme.css")
                .toURI().toURL().toExternalForm();
        scene.getStylesheets().add(themeCss);
        return scene;
    }

    private static void removeRemovedScenes()
    {
        scenesInstantiated.removeIf(scene -> !scene.getWindow().isShowing());
    }

    public static String getHelpCss()
    {
        var actualTheme = new JSONFile<>(PreferencesConfigData.class).get().theme;
        File helpCssFile = new File(TheRoundTableApplication.getAppDirectory() + "/styles/" + actualTheme + "/help.css");
        
        if (!helpCssFile.exists()) 
        {
            helpCssFile = new File(TheRoundTableApplication.getAppDirectory() + "/styles/cupertino-light/help.css");
        }

        try
        {
            return Files.readString(helpCssFile.toPath());
        }
        catch (IOException e)
        {
            Logs.getInstance().log("Help html style couldn't be loaded", e);
            return "";
        }
    }
    
    public static File getThemesDir()
    {
        return new File(TheRoundTableApplication.getAppDirectory() + "/styles");
    }
}
