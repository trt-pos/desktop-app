package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.communications.FileDownloader;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PluginsConfigData;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.pluginData.PluginData;
import org.lebastudios.theroundtable.server.Server;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        new FileDownloader().download(
                Server.BASE_URL + "/plugins/" + pluginData.pluginId + ".jar",
                new JSONFile<>(PluginsConfigData.class).get().pluginsFolder,
                "Downloading plugin update (" + pluginData.pluginName + ")",
                aferUpdate
        );
    }
}
