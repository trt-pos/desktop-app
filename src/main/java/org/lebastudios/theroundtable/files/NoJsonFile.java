package org.lebastudios.theroundtable.files;

import java.io.File;

public final class NoJsonFile extends JsonFile<NoJsonFile>
{

    @Override
    public File getFile()
    {
        return null;
    }

    @Override
    public void save()
    {
        
    }

    @Override
    public NoJsonFile load()
    {
        return this;
    }
}
