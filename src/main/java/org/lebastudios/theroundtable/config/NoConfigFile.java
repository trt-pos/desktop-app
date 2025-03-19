package org.lebastudios.theroundtable.config;

import java.io.File;

public final class NoConfigFile extends ConfigData<NoConfigFile>
{
    @Override
    public File getFile()
    {
        return null;
    }

    @Override
    public NoConfigFile load()
    {
        return this;
    }

    @Override
    public void save()
    {
    }
}
