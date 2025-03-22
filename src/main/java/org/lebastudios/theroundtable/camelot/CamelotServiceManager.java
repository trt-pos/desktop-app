package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.camelot.trtcp.Request;
import org.lebastudios.theroundtable.config.CamelotServerConfigData;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.IOException;
import java.net.URL;

public class CamelotServiceManager
{
    private static final URL SERVER_EXECUTABLE = Launcher.class.getResource("bin/camelot-linux");

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

    public Task<Void> getInitTask()
    {
        return getInitTask(new CamelotServerConfigData().load());
    }

    private Task<Void> getInitTask(CamelotServerConfigData configData)
    {
        if (serverProcess != null) throw new IllegalStateException("Server already started");

        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                if (configData.host.equals("localhost") || configData.host.equals("127.0.0.1"))
                {
                    // TODO: Generalice the execution to be able to run the server in any OS
                    ProcessBuilder pb = new ProcessBuilder(SERVER_EXECUTABLE.getPath(), configData.port + "");
                    pb.inheritIO();
                    serverProcess = pb.start();
                }

                client = new CamelotClient(configData.clientName, configData.host, configData.port);
                executeSubtask(client.getConnectTask());
                client.write(Request.ConnectRequest(client.getName()));
                
                return null;
            }
        };
    }
    
    public void stop()
    {
        if (serverProcess == null) return;

        serverProcess.destroy();
        serverProcess = null;
    }

    public Task<Void> getReloadTask(CamelotServerConfigData configData) throws IOException
    {
        stop();
        return getInitTask(configData);
    }

    public Task<Void> getReloadTask() throws IOException
    {
        return getReloadTask(new CamelotServerConfigData().load());
    }
}
