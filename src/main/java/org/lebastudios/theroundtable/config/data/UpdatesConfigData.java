package org.lebastudios.theroundtable.config.data;

import org.lebastudios.theroundtable.config.Settings;

import java.io.File;

public class UpdatesConfigData implements FileRepresentator
{
    @Override
    public File getFile()
    {
        return new File(Settings.getGlobalDir(), "updates.json");
    }
}
