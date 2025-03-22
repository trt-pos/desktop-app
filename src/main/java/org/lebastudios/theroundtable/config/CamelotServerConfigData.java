package org.lebastudios.theroundtable.config;

import java.io.File;

public class CamelotServerConfigData extends ConfigData<CamelotServerConfigData>
{
    public String clientName = "Arthur";
    public String host = "localhost";
    public int port = 1237;
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir(), "camelot-config.json");
    }
}
