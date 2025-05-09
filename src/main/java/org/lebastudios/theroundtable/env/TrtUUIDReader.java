package org.lebastudios.theroundtable.env;

import lombok.SneakyThrows;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.security.EncryptorStrategy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

public class TrtUUIDReader
{
    private static final File uuidFile = new File(Directories.internalDir(), "trt-uuid");
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
        EncryptorStrategy encryptor = EncryptorStrategy.SYSTEM;
        
        if (!uuidFile.exists())
        {
            String uuid = generateTrtUUID();
            Files.write(
                    uuidFile.toPath(), 
                    encryptor.encrypt(uuid.getBytes(StandardCharsets.UTF_8)), 
                    StandardOpenOption.CREATE_NEW
            );
        }
        
        byte[] encrypted = Files.readAllBytes(uuidFile.toPath());
        String decrypted = new String(encryptor.decrypt(encrypted), StandardCharsets.UTF_8);
        
        if (!validateTrtUUID(decrypted))
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "The UUID file is corrupted. Generating a new one."
            );
            uuidFile.delete();
            return loadTrtUUID();
        }
        
        return decrypted;
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
