package org.lebastudios.theroundtable.security;

import lombok.SneakyThrows;
import org.lebastudios.theroundtable.logs.Logs;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;

class SystemEncryptorStrategy implements EncryptorStrategy
{
    private static String getSystemIdentifier()
    {
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface ni = interfaces.nextElement();

                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) continue;

                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0)
                {
                    StringBuilder sb = new StringBuilder();

                    for (byte b : mac)
                    {
                        sb.append(String.format("%02X", b));
                    }

                    return sb.toString();
                }
            }
        }
        catch (Exception e)
        {
            Logs.getInstance().log(
                    "Failed to get the system identifier",
                    e
            );
        }

        return "0000-0000-0000";
    }

    private static SecretKey deriveKeyFromIdentifier(String identifier) throws Exception
    {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(identifier.getBytes(StandardCharsets.UTF_8));
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    @SneakyThrows
    @Override
    public byte[] encrypt(byte[] text)
    {
        String systemIdentifier = getSystemIdentifier();

        SecretKey key = deriveKeyFromIdentifier(systemIdentifier);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(text);
    }

    @SneakyThrows
    @Override
    public byte[] decrypt(byte[] text)
    {
        String systemIdentifier = getSystemIdentifier();

        SecretKey key = deriveKeyFromIdentifier(systemIdentifier);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(text);
    }
}
