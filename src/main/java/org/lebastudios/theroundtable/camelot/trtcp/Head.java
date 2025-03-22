package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>(version.toBytes());
        
        for (byte b : caller.getBytes(StandardCharsets.UTF_8)) bytes.add(b);
        
        return bytes;
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
