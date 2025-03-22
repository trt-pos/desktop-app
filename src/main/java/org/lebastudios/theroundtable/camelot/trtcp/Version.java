package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Version implements FromBytes<Version>, IntoBytes
{
    private short major;
    private short patch;
    
    @Override
    public Version fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length != 4) throw new ParseException("Invalid byte array length", 0);
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        major = buffer.getShort();
        patch = buffer.getShort();
        
        return this;
    }

    @Override
    public List<Byte> toBytes()
    {
        return new ArrayList<>();
    }

    @Override
    public String toString()
    {
        return "Version{" +
                "major=" + major +
                ", patch=" + patch +
                '}';
    }
    
    public static Version actualProtocolVersion()
    {
        return new Version((short) 1, (short) 0);
    }
}
