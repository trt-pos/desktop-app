package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.env.Platform;
import org.lebastudios.theroundtable.env.PlatformArch;
import org.lebastudios.theroundtable.env.PlatformOS;
import org.lebastudios.theroundtable.server.Server;
import org.lebastudios.theroundtable.tasks.DownloadFileTask;
import org.lebastudios.theroundtable.tasks.MoveFileTask;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
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
                    .uri(URI.create(
                            Server.BASE_URL + "/update/available?version=" + TheRoundTableApplication.getAppVersion()))
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
        new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                URI coreJar = new URI(Server.BASE_URL + "/resource/desktop-app.jar");

                PlatformArch arch = Platform.getPlatformArch();
                PlatformOS os = Platform.getPlatformOS();
                URI startBin = new URI(Server.BASE_URL
                        + "/resource/start-" + os.toString()
                        + "-" + arch.toString() + ".bin"
                );
                
                File downloadedCoreJar = executeSubtask(new DownloadFileTask(coreJar));
                File downloadedStart = executeSubtask(new DownloadFileTask(startBin));


                File coreJarSaveFile = new File(
                        TheRoundTableApplication.getAppDirectory(), 
                        "/bin/desktop-app.jar"
                );
                coreJarSaveFile.getParentFile().mkdirs();

                File startBinSaveFile = new File(
                        TheRoundTableApplication.getAppDirectory(), 
                        "start" + os.getBinaryExtension()
                );
                startBinSaveFile.getParentFile().mkdirs();

                executeSubtask(new MoveFileTask(downloadedCoreJar, coreJarSaveFile));
                executeSubtask(new MoveFileTask(downloadedStart, startBinSaveFile));
                
                return null;
            }
        }.setOnTaskComplete(_ -> afterUpdate.run())
                .executeInBackGround(true);
    }
}
