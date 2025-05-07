package org.lebastudios.theroundtable.communications;

import org.lebastudios.theroundtable.config.GlobalPreferencesConfigData;
import org.lebastudios.theroundtable.logs.Logs;

import java.net.http.HttpClient;

public class AppHttpClient
{
    private static AppHttpClient instance;

    public static AppHttpClient getInstance()
    {
        if (instance == null) instance = new AppHttpClient();

        return instance;
    }

    private HttpClient client;

    private AppHttpClient() {}

    public HttpClient getClient()
    {
        if (client == null) client = newClient();
        
        if (client.isTerminated()) 
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "HttpCLient is terminated, creating a new one");
            client = newClient();
        }
        
        return client;
    }

    public HttpClient newClient()
    {
        HttpClient.Builder client = HttpClient.newBuilder();

        var proxy = new GlobalPreferencesConfigData().load().proxyData;

        if (proxy == null || !proxy.enabled) {
            return client.build();
        }

        client.proxy(proxy.intoProxySelector());

        return client.build();
    }
}
