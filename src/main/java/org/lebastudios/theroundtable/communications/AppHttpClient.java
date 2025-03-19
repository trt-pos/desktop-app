package org.lebastudios.theroundtable.communications;

import org.lebastudios.theroundtable.config.GeneralConfigData;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.util.List;

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

        var proxy = new GeneralConfigData().load().proxyData;

        if (proxy == null || !proxy.usingProxy) {
            return client.build();
        }

        client.proxy(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                String proxyAddress = proxy.proxyAddress;
                int proxyPort = proxy.proxyPort;

                SocketAddress socketAddress = new InetSocketAddress(proxyAddress, proxyPort);

                return List.of(new Proxy(Proxy.Type.HTTP, socketAddress));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            }
        });

        return client.build();
    }
}
