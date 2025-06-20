package org.lebastudios.theroundtable;

import javafx.application.Application;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;

import java.io.File;

public class Launcher
{
    public static void main(String[] args)
    {
        // Capturing the shutdown event to release resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> AppLifeCicleEvents.onAppShutdown.invoke(null)));

        Application.launch(TheRoundTableApplication.class);
    }

    @SneakyThrows
    public static void restartAplication()
    {
        // Re-construct the command to restart the application
        String javaBin = TheRoundTableApplication.getAppDirectory() + "/jdk/bin/java";
        File jarFile = new File(TheRoundTableApplication.getAppDirectory() + "/bin/desktop-app.jar");
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-jar", jarFile.getPath());
        builder.inheritIO();
        builder.start();
        AppLifeCicleEvents.onAppCloseRequest.invoke(new WindowEvent(null, WindowEvent.WINDOW_CLOSE_REQUEST));
        System.exit(0);
    }
}
