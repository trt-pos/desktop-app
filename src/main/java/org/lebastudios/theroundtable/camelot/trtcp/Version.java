package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.nio.ByteBuffer;
import java.text.ParseException;

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
    public byte[] intoBytes()
    {
        return new byte[]{
                (byte) (major >> 8),
                (byte) major,
                (byte) (patch >> 8),
                (byte) patch
        };
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
