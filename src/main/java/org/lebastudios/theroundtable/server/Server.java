package org.lebastudios.theroundtable.server;

import org.lebastudios.theroundtable.env.Variables;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server
{
    public static final String BASE_URL = switch (Variables.getEnvironmentType()) 
    {
        case TEST, DEV -> Variables.getTestServerUrl();
        case PROD -> "https://api.rountabletpv.com/v3";
        default -> throw new IllegalStateException("Unexpected value: " + Variables.getEnvironmentType());
    };

    public static final String CENTRAL_PLUGIN_REPO_BASE_URL = switch (Variables.getEnvironmentType())
    {
        case TEST, DEV -> Variables.getTestCentralPluginRepoUrl();
        case PROD -> "https://plugins.rountabletpv.com/repo";
        default -> throw new IllegalStateException("Unexpected value: " + Variables.getEnvironmentType());
    };
    
    public static boolean isRecheable()
    {
        try
        {
            InetAddress address = InetAddress.getByName("rountabletpv.com");
            return address != null && !address.toString().isEmpty();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
