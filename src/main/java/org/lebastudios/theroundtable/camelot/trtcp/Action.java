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

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Action implements FromBytes<Action>, IntoBytes
{
    private ActionType type;
    private String module;
    private String id;

    public String getEvent()
    {
        return module + ":" + id;
    }

    @Override
    public Action fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length < 1) throw new ParseException("Invalid byte array", 0);

        type = ActionType.values()[0].fromBytes(new byte[]{bytes[0]});

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
    public byte[] intoBytes()
    {
        byte[] typeBytes = type.intoBytes();
        byte[] eventNameBytes = new FromStringToBytes(module + ":" + id).intoBytes();

        return ByteBuffer.allocate(typeBytes.length + eventNameBytes.length)
                .put(typeBytes)
                .put(eventNameBytes)
                .array();
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
