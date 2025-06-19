package org.lebastudios.theroundtable.plugins;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.DownloadFileTask;
import org.lebastudios.theroundtable.tasks.MoveFileTask;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PluginRepoIntrospector
{
    private final String repoUrl;

    public PluginRepoIntrospector(String repoUrl)
    {
        this.repoUrl = repoUrl;
    }

    public boolean ping()
    {
        return true;
    }

    public AllPluginsDataResponse getAllPluginData(String[] tags, String category, String search)
    {
        String endpoint = repoUrl + "/search?tags=" + String.join(",", tags) + "&q=" + search + "&category=" + category;

        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), AllPluginsDataResponse.class);
        }
        catch (Exception exception)
        {
            Logs.getInstance().log(
                    "An error ocurred while trying to get all the plugins in the repo",
                    exception
            );
            return null;
        }
    }

    public PluginData getPluginData(String pluginId)
    {
        String endpoint = repoUrl + "/plugin/" + pluginId + "/last/data";

        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), PluginData.class);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("An error ocurred while trying to get the last plugin version data", e);
            return null;
        }
    }

    public AllPluginsCategoriesResponse getCategories()
    {
        String endpoint = repoUrl + "/categories";

        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), AllPluginsCategoriesResponse.class);
        }
        catch (Exception e)
        {
            Logs.getInstance().log("An error ocurred while trying to get the available categories", e);
            return null;
        }
    }
    
    public String getWebIconUrl(String pluginId, Version version)
    {
        return repoUrl + "/plugin/" + pluginId + "/" + version.toString() + "/icon";
    }

    public boolean needsUpdate(String pluginId, Version version)
    {
        PluginData pluginData = getPluginData(pluginId);

        if (pluginData == null)
        {
            return false;
        }

        return new Version(pluginData.pluginVersion).compareTo(version) > 0;
    }

    public void install(String pluginId, Runnable aferUpdate)
    {
        String endpoint = repoUrl + "/plugin/" + pluginId + "/last/jar";

        new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                URI fileURI = new URI(endpoint);
                File downloadedFile = executeSubtask(new DownloadFileTask(fileURI));
                
                File saveFile = new File(intoMetadata().getLocalRepoFolder(), pluginId + ".jar");
                if (saveFile.getParentFile().mkdirs()) 
                {
                    PluginRepoIntrospector.this.intoMetadata().save();
                }

                executeSubtask(new MoveFileTask(downloadedFile, saveFile));
                aferUpdate.run();

                return null;
            }
        }.executeInBackGround(true);
    }

    public PluginRepoData intoMetadata()
    {
        return new PluginRepoData(this.repoUrl);
    }

    public static class AllPluginsDataResponse
    {
        @SerializedName("plugins-data")
        public List<PluginData> pluginsData;
    }

    public static class AllPluginsCategoriesResponse
    {
        @SerializedName("categories")
        public List<String> categories;
    }
}
