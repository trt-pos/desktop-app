package org.lebastudios.theroundtable.accounts;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LocalPasswordValidator
{
    private static final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private static final BCrypt.Verifyer verifyer = BCrypt.verifyer();
    
    public static boolean isValidFormat(String password)
    {
        return password.length() >= 8;
    }
    
    public static String hashPassword(String password)
    {
        return hasher.hashToString(12, password.toCharArray());
    }
    
    public static boolean validatePassword(String password, String hash)
    {
        return verifyer.verify(password.toCharArray(), hash).verified;
    }
}
