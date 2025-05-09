package org.lebastudios.theroundtable.security;

public interface EncryptorStrategy
{
    EncryptorStrategy SYSTEM = new SystemEncryptorStrategy();
    EncryptorStrategy CLEAR_TEXT = new NoEncryptorStrategy();
    
    byte[] encrypt(byte[] bytes);
    byte[] decrypt(byte[] bytes);
}
