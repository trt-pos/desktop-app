package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.config.CamelotServerConfigData;
import org.lebastudios.theroundtable.config.CamelotServerConfigPaneController;
import org.lebastudios.theroundtable.config.RequestConfigStageController;
import org.lebastudios.theroundtable.env.EmbeddedBinExecutor;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.IOException;

public class CamelotServiceManager
{
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

    public Task<Void> initTask()
    {
        return initTask(new CamelotServerConfigData().load());
    }

    private Task<Void> initTask(CamelotServerConfigData configData)
    {
        if (serverProcess != null) throw new IllegalStateException("Server already started");

        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Connecting to Camelot");
                
                if (configData.host.equals("localhost") || configData.host.equals("127.0.0.1"))
                {
                    serverProcess = new EmbeddedBinExecutor().execute(
                            CorePlugin.class,
                            "camelot",
                            configData.port + ""
                    );
                }

                // Create the client object to be connected to the server
                CamelotClient tmpClient = new CamelotClient(configData.clientName, configData.host, configData.port);
                // Asigning the callback handler to the client to handle the events callbacks
                tmpClient.setCallbacksHandler(CamelotEventsManager.getInstance().callbacksHandler);
                // Execute the connection task to finally connect to the server
                executeSubtask(tmpClient.connectTask());
                
                // Assign the client object to the class variable if the connection was successful
                client = tmpClient;
                
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

    public Task<Void> reloadTask(CamelotServerConfigData configData)
    {
        stop();
        return initTask(configData);
    }

    public Task<Void> reloadTask() throws IOException
    {
        return reloadTask(new CamelotServerConfigData().load());
    }
    
    CamelotClient getPersistentClient()
    {
        while (client == null)
        {
            new RequestConfigStageController(new CamelotServerConfigPaneController())
                    .instantiate(true);
        }
        
        return client;
    }
}
