package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.communications.FileDownloader;
import org.lebastudios.theroundtable.server.Server;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        new FileDownloader().download(
                Server.BASE_URL + "/update/desktop-app.jar",
                TheRoundTableApplication.getAppDirectory() + "/bin",
                "Downloading app update",
                afterUpdate
        );
    }
}
