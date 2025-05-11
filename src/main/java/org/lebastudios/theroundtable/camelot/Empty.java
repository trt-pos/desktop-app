package org.lebastudios.theroundtable.camelot;

import java.text.ParseException;

public final class Empty implements IntoBytes, FromBytes<Empty>
{
    @Override
    public Empty fromBytes(byte[] bytes) throws ParseException
    {
        return new Empty();
    }

    @Override
    public byte[] intoBytes()
    {
        return new byte[] {};
    }
}
