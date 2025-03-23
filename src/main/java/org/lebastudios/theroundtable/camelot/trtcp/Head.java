package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.FromStringToBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Head implements FromBytes<Head>, IntoBytes
{
    private Version version;
    private String caller;
    
    @Override
    public Head fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length < 4) throw new ParseException("Invalid byte array", 0);
        
        version = new Version().fromBytes(Arrays.copyOfRange(bytes, 0, 4));
        
        if (bytes.length == 4)
        {
            caller = "";
            return this;
        }
        
        caller = new String(Arrays.copyOfRange(bytes, 4, bytes.length), StandardCharsets.UTF_8);
        
        return this;
    }

    @Override
    public byte[] intoBytes()
    {
        byte[] versionBytes = version.intoBytes();
        byte[] callerBytes = new FromStringToBytes(caller).intoBytes();
        
        return ByteBuffer.allocate(versionBytes.length + callerBytes.length)
                .put(versionBytes)
                .put(callerBytes)
                .array();
    }

    @Override
    public String toString()
    {
        return "Head{" +
                "version=" + version +
                ", caller='" + caller + '\'' +
                '}';
    }
}
