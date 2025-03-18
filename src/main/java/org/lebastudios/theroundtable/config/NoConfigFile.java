package org.lebastudios.theroundtable.config;

import java.io.File;

public final class NoConfigFile extends ConfigData<NoConfigFile>
{
    @Override
    public File getFile()
    {
        return null;
    }
}
