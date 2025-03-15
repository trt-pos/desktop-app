package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import javafx.concurrent.Worker;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.DownloadFileTask;
import org.lebastudios.theroundtable.server.Server;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class Updates
{
    public static boolean isUpdateAvailable()
    {
        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(Server.BASE_URL + "/update/available?version=" + TheRoundTableApplication.getAppVersion()))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            HashMap<String, Boolean> responseMap = new Gson().fromJson(response.body(), HashMap.class);

            return responseMap.get("response");
        }
        catch (Exception e)
        {
            System.err.println("An error ocurred while trying to get the available update: " + e.getMessage());
            return false;
        }
    }

    public static void donwloadAppLastVersion(Runnable afterUpdate)
    {
        // TODO: Make a task for saving a downloaded task
        try
        {
            URI fileURI = new URI(Server.BASE_URL + "/update/desktop-app.jar");

            Task<File> task = new DownloadFileTask(fileURI);
            task.stateProperty().addListener((_, _, newValue) ->
            {
                if (newValue == Worker.State.SUCCEEDED)
                {
                    File saveFile = new File(TheRoundTableApplication.getAppDirectory(), "/bin/desktop-app.jar");
                    saveFile.getParentFile().mkdirs();
                    try
                    {
                        Files.copy(task.getValue().toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Files.delete(task.getValue().toPath());
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }

                    afterUpdate.run();
                }
            });
            task.executeInBackGround(true);
        }
        catch (Exception exception)
        {
            Logs.getInstance().log("Error installing core update", exception);
        }
    }
}
