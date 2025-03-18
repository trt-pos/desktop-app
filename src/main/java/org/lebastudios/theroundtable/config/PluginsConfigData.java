package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.TheRoundTableApplication;

import java.io.File;

public class PluginsConfigData extends ConfigData<PluginsConfigData>
{
    public String pluginsFolder = TheRoundTableApplication.getUserDirectory() + "/plugins/";

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir() + "/plugins.json");
    }
}
