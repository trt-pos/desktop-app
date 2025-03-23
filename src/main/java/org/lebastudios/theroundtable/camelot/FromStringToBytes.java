package org.lebastudios.theroundtable.camelot;

import java.util.List;

public class FromStringToBytes implements IntoBytes
{
    private final String value;

    public FromStringToBytes(String value) {this.value = value;}

    @Override
    public List<Byte> toBytes()
    {
        byte[] bytes = value.getBytes();
        List<Byte> byteList = new java.util.ArrayList<>(bytes.length);
        for (byte b : bytes) byteList.add(b);
        return byteList;
    }
}
