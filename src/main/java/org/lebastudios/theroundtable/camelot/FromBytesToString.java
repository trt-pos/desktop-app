package org.lebastudios.theroundtable.camelot;

import java.nio.charset.StandardCharsets;

public class FromBytesToString implements FromBytes<String>
{
    @Override
    public String fromBytes(byte[] bytes)
    {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
