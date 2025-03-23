package org.lebastudios.theroundtable.camelot;

import java.nio.charset.StandardCharsets;

public final class FromStringToBytes implements IntoBytes
{
    private final String value;

    public FromStringToBytes(String value) {this.value = value;}

    @Override
    public byte[] intoBytes()
    {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
