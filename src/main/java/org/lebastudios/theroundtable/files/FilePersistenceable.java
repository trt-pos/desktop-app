package org.lebastudios.theroundtable.files;

import java.io.File;

public interface FilePersistenceable<T>
{
    File getFile();

    void save();
    
    T load();
}
