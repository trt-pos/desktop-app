package org.lebastudios.theroundtable.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZipTask extends Task<Void>
{
    private final File src;
    private final File dest;
    
    public CreateZipTask(File src, File dest)
    {
        super("zip.png");
        this.src = src;
        this.dest = dest;
    }

    @Override
    protected Void call() throws Exception
    {
        try (var zos = new ZipOutputStream(new FileOutputStream(dest)))
        {
            updateTitle("Zip creation");
            updateTitle("Creating zip for " + src.getName());
            var buffer = new byte[1024];
            var files = src.listFiles();
            
            assert files != null;
            
            long dirSize = 0;
            for (var file : files)
            {
                dirSize += file.length();
            }
            
            long copied = 0;
            
            for (var file : files)
            {
                try (var fis = new FileInputStream(file))
                {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0)
                    {
                        zos.write(buffer, 0, length);
                        copied += length;
                        updateProgress(copied, dirSize);
                    }
                    zos.closeEntry();
                }
                catch (Exception _) {}
            }
        }
        return null;
    }
}
