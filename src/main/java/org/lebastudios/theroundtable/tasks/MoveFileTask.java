package org.lebastudios.theroundtable.tasks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MoveFileTask extends Task<Void>
{
    private final File src;
    private final File dest;
    
    public MoveFileTask(File src, File dest)
    {
        super("move.png");
        this.src = src;
        this.dest = dest;
    }
    
    @Override
    protected Void call() throws Exception
    {
        updateTitle("Moving file");
        updateMessage("Moving file to the correct location...");
        updateProgress(0, 1);

        Files.copy(src.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(src.toPath());
        
        updateProgress(1, 1);
        
        return null;
    }
}
