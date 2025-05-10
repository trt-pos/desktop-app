package org.lebastudios.theroundtable.env;

import com.github.javakeyring.PasswordAccessException;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.security.KeyringManager;

import java.util.Locale;
import java.util.UUID;

public class TrtUUIDReader
{
    private static String uuid = null;

    public String getTrtUUID()
    {
        if (uuid == null)
        {
            uuid = loadTrtUUID();
        }

        return uuid;
    }

    @SneakyThrows
    private String loadTrtUUID()
    {
        String trtUuid = KeyringManager.getInstance().getSecret(CorePlugin.getInstance(), "TRT_UUID").orElseGet(() ->
        {
            String newUuid = generateTrtUUID();
            try
            {
                KeyringManager.getInstance().setSecret(CorePlugin.getInstance(), "TRT_UUID", newUuid);
            }
            catch (PasswordAccessException e)
            {
                Logs.getInstance().log(
                        "Failed to set the new UUID in the keyring",
                        e
                );
                return  null;
            }
            return newUuid;
        });

        assert trtUuid != null;
        
        if (!validateTrtUUID(trtUuid))
        {
            trtUuid = generateTrtUUID();
            KeyringManager.getInstance().deleteSecret(CorePlugin.getInstance(), "TRT_UUID");
            KeyringManager.getInstance().setSecret(CorePlugin.getInstance(), "TRT_UUID", trtUuid);
        }
        
        return trtUuid;
    }

    private boolean validateTrtUUID(String uuid)
    {
        String[] parts = uuid.split("::");
        if (parts.length != 3) return false;

        String application = parts[0];
        String country = parts[1];
        String uuidPart = parts[2];

        if (!application.equals("TRT")) return false;
        if (country.length() != 2) return false;

        try
        {
            UUID.fromString(uuidPart);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }

        return true;
    }
    
    private String generateTrtUUID()
    {
        String aplication = "TRT";
        String country = Locale.getDefault().getCountry().isEmpty() ? "XX" : Locale.getDefault().getCountry();
        String uuid = String.valueOf(UUID.randomUUID());
        
        return String.format("%s::%s::%s", aplication, country, uuid);
    }
}
