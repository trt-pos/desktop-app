package org.lebastudios.theroundtable.communications;

import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.entities.AppInstallation;
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
        String subnet = Database.getInstance().connectQuery(session ->
        {
            return AppInstallation.thisInstalation(session).getSubnet();
        });

        try
        {
            String[] parts = subnet.split("/");
            InetAddress subnetAddress = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] subnetBytes = subnetAddress.getAddress();
            int subnetMask = -1 << (32 - prefixLength);

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;

                for (InterfaceAddress addr : iface.getInterfaceAddresses())
                {
                    InetAddress inetAddr = addr.getAddress();
                    if (!(inetAddr instanceof Inet4Address)) continue;

                    byte[] ipBytes = inetAddr.getAddress();

                    int ipInt = byteArrayToInt(ipBytes);
                    int subnetInt = byteArrayToInt(subnetBytes);

                    if ((ipInt & subnetMask) == (subnetInt & subnetMask))
                    {
                        return inetAddr.getHostAddress();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Logs.getInstance().log("Error finding local IP in subnet", ex);
        }

        return "127.0.0.1";
    }

    private static int byteArrayToInt(byte[] bytes)
    {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}
