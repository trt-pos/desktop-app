package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import javafx.concurrent.Worker;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.tasks.DownloadFileTask;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PluginsConfigData;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginData;
import org.lebastudios.theroundtable.server.Server;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class Plugins
{
    public static PluginData[] getAllAvailablePluginsData()
    {
        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(Server.BASE_URL + "/plugins/pluginsData"))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
            {
                Logs.getInstance().log(
                        Logs.LogType.ERROR,
                        "An error ocurred while trying to get the available plugins data: " + response.body()
                );
                return new PluginData[0];
            }

            return new Gson().fromJson(response.body(), PluginData[].class);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("An error ocurred while trying to get the available plugins data", e);
            return null;
        }
    }

    public static PluginData getAvailablePluginData(String pluginId)
    {
        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(Server.BASE_URL + "/plugins/" + pluginId + ".jar.json"))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
            {
                Logs.getInstance().log(
                        Logs.LogType.ERROR,
                        "An error ocurred while trying to get the plugin data: " + response.body()
                );
                return null;
            }

            return new Gson().fromJson(response.body(), PluginData.class);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("An error ocurred while trying to get the plugin data", e);
            return null;
        }
    }

    public static boolean needsUpdate(PluginData pluginData)
    {
        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(Server.BASE_URL + "/plugins/" + pluginData.pluginId + "/updateable?version=" +
                            pluginData.pluginVersion))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            HashMap<String, Boolean> responseMap = new Gson().fromJson(response.body(), HashMap.class);

            return responseMap.get("response");
        }
        catch (Exception e)
        {
            Logs.getInstance().log("An error ocurred while trying to get the available update", e);
            return false;
        }
    }

    public static void install(PluginData pluginData, Runnable aferUpdate)
    {
        try
        {
            URI fileURI = new URI(Server.BASE_URL + "/plugins/" + pluginData.pluginId + ".jar");

            Task<File> task = new DownloadFileTask(fileURI);
            task.stateProperty().addListener((_, _, newValue) ->
            {
                if (newValue == Worker.State.SUCCEEDED)
                {
                    File saveFile = new File(new JSONFile<>(PluginsConfigData.class).get().pluginsFolder, pluginData.pluginId + ".jar");
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
                    
                    aferUpdate.run();
                }
            });
            task.executeInBackGround(true);
        }
        catch (Exception exception)
        {
            Logs.getInstance().log("Error installing plugin", exception);
        }
    }
}
