package org.lebastudios.theroundtable.files;

import java.io.File;

public interface FilePersistanceable<T>
{
    File getFile();

    void save();
    
    T load();
}
