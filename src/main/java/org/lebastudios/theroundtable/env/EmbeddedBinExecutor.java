package org.lebastudios.theroundtable.env;

import org.controlsfx.tools.Platform;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class EmbeddedBinExecutor
{
    // This is a map of the extracted binaries from the jar file.
    // The key is <iPlugin class name>:<binName>, and the value is the absolute path to the extracted bin file.
    private static final HashMap<String, String> jarExtractedBins = new HashMap<>();
    
    public <T extends IPlugin> Process execute(Class<T> pluginImpl, String binName, String... args) throws IOException
    {
        return execute(pluginImpl, binName, false, args);
    }
    
    /**
     * Extracts a binary file from the jar file to a temporary directory (?).
     * @param binName The binary name. This method will 
     * add -{os}{extension} to the binary name and will expect to find the corresponding binary file in the bin folder.
     * @param args The arguments to pass to the bin.
     * @return The process of the bin or null if the bin couldn't be executed.
     * @throws Exception
     */
    public <T extends IPlugin> Process execute(Class<T> pluginImpl, String binName, boolean asRoot, String... args) throws IOException
    {
        String binId = PluginsManager.getInstance().getPluginOf(pluginImpl).orElseThrow().getPluginData().pluginId + ":" + binName;
        String finalBinName = binName + switch (Platform.getCurrent())
        {
            case WINDOWS -> "-win.exe";
            case OSX -> "-macos";
            case UNIX -> "-linux";
            case UNKNOWN -> throw new IllegalStateException("Unknown platform: " + Platform.getCurrent());
        };

        URL binURL = pluginImpl.getResource("bin/" + finalBinName);

        if (binURL == null)
        {
            throw new RuntimeException("The bin file does not exist: " + finalBinName);
        }

        if (!jarExtractedBins.containsKey(binId))
        {
            Path out = Paths.get(
                    Directories.getTempDir().getPath(),
                    finalBinName
            );
            try (InputStream is = binURL.openStream())
            {
                Files.copy(is, out, StandardCopyOption.REPLACE_EXISTING);
                File outFile = out.toFile();
                outFile.setExecutable(true, !asRoot);
                jarExtractedBins.put(binId, outFile.getAbsolutePath());
            }
        }

        File binFile = new File(jarExtractedBins.get(binId));

        if (!binFile.exists() || !binFile.isFile())
        {
            throw new RuntimeException("The bin file does not exist: " + binFile.getPath());
        }

        if (!binFile.isAbsolute())
        {
            throw new RuntimeException("The bin file does not refer to an absolute path: " + binFile.getPath());
        }

        String[] command = new String[args.length + 1];

        command[0] = binFile.getAbsolutePath();
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        return pb.start();
    }
}
