package org.lebastudios.theroundtable.tasks;

import lombok.SneakyThrows;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.env.Platform;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DownloadFileTask extends Task<File>
{
    private final URI fileURI;
    
    public DownloadFileTask(URI fileURI)
    {
        super("download.png");
        this.fileURI = fileURI;
    }

    @Override
    protected File call() throws Exception
    {
        updateTitle("Server connection");
        
        updateMessage("Communicating with the server...");
        updateProgress(0, 1);
        
        HttpClient client = AppHttpClient.getInstance().getClient();
        HttpResponse<InputStream> response = client.send(
                HttpRequest.newBuilder().uri(fileURI).build(),
                HttpResponse.BodyHandlers.ofInputStream()
        );

        int responseCode = response.statusCode();
        if (responseCode != HttpURLConnection.HTTP_OK)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR, 
                    "Failed to download file " + fileURI + ". Response code: " + responseCode
            );
            throw new IOException("Failed to download file " + fileURI + ". Response code: " + responseCode);
        }

        updateMessage("Preparing environment to save file...");
        String fileName = fileURI.toString().substring(fileURI.toString().lastIndexOf("/") + 1);

        var tempFile = new File(Platform.getTempDir(), fileName);
        
        if (tempFile.exists() && !tempFile.delete()) 
        {
            throw new IOException("Failed to delete temporary file " + tempFile);
        }

        int totalBytes = Integer.parseInt(response.headers().map().get("Content-Length").getFirst());
        updateTitle("File download");
        updateMessage("Downloading...");

        try (InputStream inputStream = response.body();
             FileOutputStream outputStream = new FileOutputStream(tempFile))
        {
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);

                if (totalBytes == -1) continue;

                updateProgress(outputStream.getChannel().size(), totalBytes);
            }

            outputStream.flush();
        }
        
        return tempFile;
    }
    
    public boolean download(String fileURL, String saveDir, String taskTitle, Runnable afterDownload)
    {
        try
        {
            HttpClient client = AppHttpClient.getInstance().getClient();
            
            HttpResponse<InputStream> response = client.send(
                    HttpRequest.newBuilder().uri(URI.create(fileURL)).build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            int responseCode = response.statusCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
            {
                Logs.getInstance().log(Logs.LogType.ERROR, "Failed to download file " + fileURL + ". Response code: " + responseCode);
                return false;
            }

            String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);

            var saveFile = new File(saveDir + File.separator + fileName);
            var tempFile = new File(TheRoundTableApplication.getUserDirectory() + "/.tmp/" + fileName);

            if (!tempFile.exists()) tempFile.getParentFile().mkdirs();

            var appTask = new Task<Void>("download.png")
            {
                @SneakyThrows
                @Override
                protected Void call()
                {
                    int totalBytes = Integer.parseInt(response.headers().map().get("Content-Length").getFirst());
                    updateTitle(taskTitle);
                    updateMessage("Downloading...");

                    try (InputStream inputStream = response.body();
                         FileOutputStream outputStream = new FileOutputStream(tempFile))
                    {
                        int bytesRead;
                        byte[] buffer = new byte[4096];
                        while ((bytesRead = inputStream.read(buffer)) != -1)
                        {
                            outputStream.write(buffer, 0, bytesRead);

                            if (totalBytes == -1) continue;

                            updateProgress(outputStream.getChannel().size(), totalBytes);
                        }

                        outputStream.flush();
                    }
                    catch (Exception e)
                    {
                        Logs.getInstance().log("An error ocurred while downloading the file", e);
                        return null;
                    }

                    updateMessage("Saving file...");
                    saveFile.getParentFile().mkdirs();
                    Files.copy(tempFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(tempFile.toPath());
                    
                    afterDownload.run();
                    return null;
                }
            };
            
            TaskManager.getInstance().startNewBackgroundTask(appTask, false);
        }
        catch (IOException | InterruptedException e)
        {
            Logs.getInstance().log("Error downloading file " + fileURL, e);
            return false;
        }

        return true;
    }
}
