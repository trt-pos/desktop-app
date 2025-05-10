package org.lebastudios.theroundtable.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.communications.LocalIpFinder;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.env.TrtUUIDReader;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.plugins.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_app_installation")
@Getter
@Setter
@NoArgsConstructor
public class AppInstallation
{
    static {
        AccountEvents.OnAccountLogIn.addListener(account ->
        {
            Database.getInstance().connectTransaction(session ->
            {
                var appInstalation = AppInstallation.thisInstalation(session);
                appInstalation.setLastAccount(account);
                session.merge(appInstalation);
            });
        });
        
        AccountEvents.OnAccountLogOutAfter.addListener(() ->
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
    private String trtUuid;
    
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

    public AppInstallation(String trtUuid, Version version, LocalDateTime createdAt, LocalDateTime updatedAt,
            Status status)
    {
        this.trtUuid = trtUuid;
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
            this.setIp(new LocalIpFinder().find());
        }
    }

    public void setLastAccount(Account lastAccount)
    {
        this.lastAccount = lastAccount;
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status
    {
        ACTIVE,
        INACTIVE,
        DISABLED,
        STANBY
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
