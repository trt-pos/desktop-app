package org.lebastudios.theroundtable.files;

import java.io.File;

public final class NoFile implements FilePersistenceable<NoFile>
{
    @Override
    public final File getFile()
    {
        return null;
    }

    @Override
    public final void save()
    {

    }

    @Override
    public final NoFile load()
    {
        return this;
    }
}
