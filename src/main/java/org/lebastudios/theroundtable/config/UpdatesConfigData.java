package org.lebastudios.theroundtable.config;

import java.io.File;

public class UpdatesConfigData extends ConfigData<UpdatesConfigData>
{
    public boolean checkUpdates = false;
    
    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir(), "updates.json");
    }
}
