package org.lebastudios.theroundtable.security;

class NoEncryptorStrategy implements EncryptorStrategy
{
    public byte[] encrypt(byte[] text)
    {
        return text;
    }

    public byte[] decrypt(byte[] text)
    {
        return text;
    }
}
