package org.lebastudios.theroundtable.server;

import org.lebastudios.theroundtable.env.Variables;

import java.net.*;

public class Server
{
    public static final String BASE_URL = switch (Variables.getEnvironmentType()) 
    {
        case TEST, DEV -> Variables.getTestServerUrl();
        case PROD -> "https://lebastudios.org/api/v1/theroundtable";
        default -> throw new IllegalStateException("Unexpected value: " + Variables.getEnvironmentType());
    };

    public static boolean isRecheable()
    {
        try
        {
            InetAddress address = InetAddress.getByName("lebastudios.org");
            return address != null && !address.toString().isEmpty();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
