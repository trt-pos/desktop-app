package org.lebastudios.theroundtable.config;

import java.io.File;

public class GeneralConfigData extends ConfigData<GeneralConfigData>
{
    public boolean setupComplete = false;
    public ProxyData proxyData = new ProxyData();
    public ProxyData localProxyData = new ProxyData();

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir(), "general-settings.json");
    }

    public static class ProxyData
    {
        public boolean usingProxy = false;
        public String proxyAddress = "";
        public int proxyPort = 0;
        public String proxyUsername = "";
        public String proxyPassword = "";
    }
}
