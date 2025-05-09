package org.lebastudios.theroundtable.communications;

import org.lebastudios.theroundtable.logs.Logs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class LocalIpFinder
{
    public String find()
    {
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                {
                    continue;
                }

                for (InterfaceAddress addr : iface.getInterfaceAddresses())
                {
                    InetAddress inetAddr = addr.getAddress();
                    if (inetAddr instanceof Inet4Address && !inetAddr.isLoopbackAddress())
                    {
                        System.out.println("Found local IP address: " + inetAddr.getHostAddress());
                        return inetAddr.getHostAddress();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Logs.getInstance().log(
                    "Error finding local IP address",
                    ex
            );
        }

        return "127.0.0.1";
    }
}
