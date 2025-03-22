package org.lebastudios.theroundtable.camelot;

import lombok.Getter;
import org.lebastudios.theroundtable.camelot.trtcp.FromBytes;
import org.lebastudios.theroundtable.camelot.trtcp.IntoBytes;
import org.lebastudios.theroundtable.camelot.trtcp.Request;
import org.lebastudios.theroundtable.camelot.trtcp.Response;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.text.ParseException;
import java.util.List;

class CamelotClient
{
    @Getter private final String name;
    private final String host;
    private final int port;
    
    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private Response lastResponse;
    
    public CamelotClient(String name, String host, int port)
    {
        this.name = name;
        this.host = host;
        this.port = port;
    }
    
    public Task<Void> getConnectTask()
    {
        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                int retries = 0;
                boolean success = false;
                int milisToWait = 1000;
                while (retries < 3 && !success)
                {
                    int actualTimeToWait = milisToWait * retries + 25;
                    try
                    {
                        Thread.sleep(actualTimeToWait);
                        socket = new Socket(host, port);
                        success = true;
                    }
                    catch (ConnectException exception)
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Failed to connect to Camelot, retrying in " + actualTimeToWait / 1000 + " seconds"
                        );
                        retries++;
                    }
                }

                if (!success)
                {
                    throw new ConnectException("Failed to connect to Camelot");
                }

                in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());
                return null;
            }
        };
    }
    
    public void disconnect() throws IOException
    {
        if (socket == null) return;
        
        socket.close();
        socket = null;
    }
    
    public void write(IntoBytes data) throws IOException
    {
        List<Byte> byteList = data.toBytes();
        byte[] bytes = new byte[byteList.size()];
        
        for (int i = 0; i < byteList.size(); i++) bytes[i] = byteList.get(i);
        
        out.write(bytes);
        out.flush();
        // TODO: return response use of wait notify
    }
    
    // TODO: have a thread reading everything and firing events or saving responses
    public <T extends FromBytes<T>> T read() throws IOException, ParseException
    {
        byte[] bytes = in.readAllBytes();
        
        return switch (bytes[0])
        {
            case 0 -> (T) new Request().fromBytes(bytes);
            case 1 -> (T) new Response().fromBytes(bytes);
            default -> throw new IOException("Invalid byte");
        };
    }
}
