package org.lebastudios.theroundtable.config;

import java.io.File;

public class CamelotServerConfigData extends ConfigData<CamelotServerConfigData>
{
    public boolean defaultConfig = true;
    public String host = "localhost";
    public int port = 1237;
    
    public boolean usesLocalServer()
    {
        return host.equals("localhost") || host.equals("127.0.0.1") || host.isBlank();
    }
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir(), "camelot-config.json");
    }
}
