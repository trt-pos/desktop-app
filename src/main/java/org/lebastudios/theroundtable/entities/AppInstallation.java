package org.lebastudios.theroundtable.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.database.PluginTable;
import org.lebastudios.theroundtable.env.TrtUUIDReader;
import org.lebastudios.theroundtable.accounts.AccountEvents;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.Version;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.util.Enumeration;

@Entity
@PluginTable(name = "app_installation")
@Getter
@Setter
@NoArgsConstructor
public class AppInstallation
{
    static {
        AccountEvents.onAccountLogIn.addListener(account ->
        {
            Database.getInstance().connectTransaction(session ->
            {
                var appInstalation = AppInstallation.thisInstalation(session);
                appInstalation.setLastAccount(account);
                session.merge(appInstalation);
            });
        });
        
        AccountEvents.onAccountLogOutAfter.addListener((_) ->
        {
            Database.getInstance().connectTransaction(session ->
            {
                var appInstalation = AppInstallation.thisInstalation(session);
                appInstalation.setLastAccount(null);
                session.merge(appInstalation);
            });
        });
    }
    
    @Id
    @Column(name = "uuid", nullable = false)
    private String uuid;
    
    @Column(name = "name", nullable = false)
    private String name = "Unknown";
    
    @Convert(converter = VersionConverter.class)
    @Column(name = "version", nullable = false)
    private Version Version;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "ip")
    private String ip;
    
    @Column(name = "subnet")
    private String subnet = "192.168.1.0/24";
    
    @Column(name = "is_master")
    private boolean isMaster;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_account_id")
    private Account lastAccount;

    public AppInstallation(String uuid, Version version, LocalDateTime createdAt, LocalDateTime updatedAt,
            Status status)
    {
        this.uuid = uuid;
        Version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        
        if (status == Status.ACTIVE)
        {
            updateIp();
        }
    }

    public void setSubnet(String subnet)
    {
        this.subnet = subnet;
        updateIp();
    }
    
    public void setLastAccount(Account lastAccount)
    {
        this.lastAccount = lastAccount;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIp()
    {
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
                        ip = inetAddr.getHostAddress();
                        return;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Logs.getInstance().log("Error finding local IP in subnet", ex);
        }

        ip = "127.0.0.1";
    }

    private static int byteArrayToInt(byte[] bytes)
    {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
    
    public enum Status
    {
        ACTIVE,
        INACTIVE,
        DISABLED,
        STANBY
    }

    public static AppInstallation thisInstalation()
    {
        return Database.getInstance().connectQuery(session ->
        {
            return session.get(AppInstallation.class, new TrtUUIDReader().getTrtUUID());
        });
    }
    
    public static AppInstallation thisInstalation(Session session)
    {
        return session.get(AppInstallation.class, new TrtUUIDReader().getTrtUUID());
    }
    
    public static AppInstallation defaultInstalation()
    {
        return new AppInstallation(
                new TrtUUIDReader().getTrtUUID(),
                new Version(TheRoundTableApplication.getAppVersion()),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Status.ACTIVE
        );
    }
    
    @Converter
    private static class VersionConverter implements AttributeConverter<Version, String>
    {
        @Override
        public String convertToDatabaseColumn(Version version) {
            return version != null ? version.toString() : null;
        }
        
        @Override
        public Version convertToEntityAttribute(String dbData) {
            return dbData != null ? new Version(dbData) : null;
        }
    }

}
