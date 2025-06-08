package org.lebastudios.theroundtable.env;

import java.io.File;
import java.io.IOException;

public class Directories
{
    public static String getHomeDir()
    {
        String environment = System.getenv("TRT_HOME");

        String homeDir = System.getProperty("user.home") + File.separator + ".round-table";

        if (environment != null)
        {
            try
            {
                File provisionalHomeDir = new File(environment);
                provisionalHomeDir.createNewFile();
                if (!provisionalHomeDir.exists()) throw new RuntimeException("TRT_HOME file wasn't created as expected");

                homeDir = environment;

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return homeDir + (Variables.isDev() ? "-dev" : "");
    }
    
    public static File getTempDir()
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "theroundtable");
        
        if (!file.exists()) file.mkdirs();
        
        return file;
    }

    public static String getPersistedImagesDir()
    {
        var dir = getHomeDir() + "/saved-images";

        var file = new File(dir);

        if (!file.exists()) file.mkdirs();

        return dir;
    }
}
