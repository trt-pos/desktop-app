package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Action implements FromBytes<Action>, IntoBytes
{
    private ActionType type;
    private String module;
    private String id;
    
    @Override
    public Action fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length < 1) throw new ParseException("Invalid byte array", 0);
        
        type = ActionType.values()[0].fromBytes(new byte[bytes[0]]);
        
        if (bytes.length == 1) 
        {
            module = "";
            id = "";
            return this;
        }
        
        String eventName = new String(bytes, 1, bytes.length - 1, StandardCharsets.UTF_8);
        String[] parts = eventName.split(":");
        
        if (parts.length != 2) throw new ParseException("Invalid byte array", 0);
        
        module = parts[0];
        id = parts[1];
        
        return this;
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>(type.toBytes());

        byte[] eventName = (module + ":" + id).getBytes(StandardCharsets.UTF_8);
        for (byte b : eventName) bytes.add(b);
        
        return bytes;
    }

    @Override
    public String toString()
    {
        return "Action{" +
                "type=" + type +
                ", module='" + module + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
