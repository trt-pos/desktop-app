package org.lebastudios.theroundtable.tasks;

import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.env.Platform;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
}
