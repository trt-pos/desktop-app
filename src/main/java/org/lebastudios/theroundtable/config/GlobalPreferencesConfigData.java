package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.locale.Currency;
import org.lebastudios.theroundtable.locale.Language;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class GlobalPreferencesConfigData extends ConfigData<GlobalPreferencesConfigData>
{
    public Language language = Language.getDefault();
    public Currency currency = Currency.getDefault();
    public String dateTimeFormatter = "dd/MM/yyyy HH:mm:ss";
    public String timeFormatter = "HH:mm:ss";
    public String dateFormatter = "dd/MM/yyyy";

    public ProxyData proxyData = new ProxyData();
    public ProxyData intraNetProxyData = new ProxyData();

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir(), "global-preferences.json");
    }

    public static class ProxyData
    {
        public boolean enabled = false;

        public String proxyAddress = "192.168.0.11";
        public int proxyPort = 3128;

        public String proxyUsername = null;
        public String proxyPassword = null;

        public Proxy intoProxy()
        {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
        }

        public ProxySelector intoProxySelector()
        {
            return new ProxySelector()
            {
                @Override
                public List<Proxy> select(URI uri)
                {
                    return List.of(intoProxy());
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
            };
        }
    }
}
