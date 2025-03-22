package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.camelot.trtcp.Request;
import org.lebastudios.theroundtable.camelot.trtcp.Response;
import org.lebastudios.theroundtable.config.CamelotServerConfigData;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.text.ParseException;

public class CamelotServiceManager
{
    private static final URL SERVER_EXECUTABLE = CamelotServiceManager.class.getResource("camelot");

    private static CamelotServiceManager instance;

    public static CamelotServiceManager getInstance()
    {
        if (instance == null) instance = new CamelotServiceManager();

        return instance;
    }

    private CamelotClient client;
    private Process serverProcess;

    private CamelotServiceManager()
    {
        AppLifeCicleEvents.OnAppClose.addListener(_ -> stop());
    }

    public void init() throws IOException
    {
        init(new CamelotServerConfigData().load());
    }

    private void init(CamelotServerConfigData configData) throws IOException
    {
        if (serverProcess != null) throw new IllegalStateException("Server already started");

        if (configData.host.equals("localhost") || configData.host.equals("127.0.0.1"))
        {
            // TODO: Generalice the execution to be able to run the server in any OS
            ProcessBuilder pb = new ProcessBuilder(SERVER_EXECUTABLE.getPath(), configData.port + "");
            pb.inheritIO();
            serverProcess = pb.start();
        }
        
        client = new CamelotClient(configData.clientName, configData.host, configData.port);
        client.connect();
        client.write(Request.ConnectRequest(client.getName()));
    }
    
    public void stop()
    {
        if (serverProcess == null) return;

        serverProcess.destroy();
        serverProcess = null;
    }

    public void reload(CamelotServerConfigData configData)  throws IOException
    {
        stop();
        init(configData);
    }

    public void reload()  throws IOException
    {
        reload(new CamelotServerConfigData().load());
    }
}
