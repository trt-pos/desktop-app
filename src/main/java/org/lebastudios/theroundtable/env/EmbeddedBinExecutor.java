package org.lebastudios.theroundtable.env;

import org.controlsfx.tools.Platform;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class EmbeddedBinExecutor
{
    // This is a map of the extracted binaries from the jar file. The key is the absolute path to the bin file inside
    // the jar file, and the value is the absolute path to the extracted bin file.
    private static final HashMap<String, String> jarExtractedBins = new HashMap<>();

    /**
     * Extracts a binary file from the jar file to a temporary directory (?).
     * @param binName The binary name. This method will 
     * add -{os}{extension} to the binary name and will expect to find the corresponding binary file in the bin folder.
     * @param args The arguments to pass to the bin.
     * @return The process of the bin or null if the bin couldn't be executed.
     * @throws Exception
     */
    public <T extends IPlugin> Process execute(Class<T> resources, String binName, String... args) throws IOException
    {
        String finalBinName = binName + switch (Platform.getCurrent())
        {
            case WINDOWS -> "-win.exe";
            case OSX -> "-macos";
            case UNIX -> "-linux";
            case UNKNOWN -> throw new IllegalStateException("Unknown platform: " + Platform.getCurrent());
        };

        URL binURL = resources.getResource("bin/" + finalBinName);
        
        if (binURL == null) 
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "The bin file does not exist: " + finalBinName
            );
            return null;
        }
        
        File binFile = new File(binURL.getPath());
        if (!binFile.exists() || !binFile.isFile()) 
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "The bin file does not exist or is not a file: " + binURL
            );
            return null;
        }
        
        if (!binFile.isAbsolute()) 
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "The bin file path is not absolute: " + binURL
            );
            return null;
        }
        
        String[] command = new String[args.length + 1];
        
        command[0] = binFile.getAbsolutePath();
        System.arraycopy(args, 0, command, 1, args.length);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        return pb.start();
    }
}
