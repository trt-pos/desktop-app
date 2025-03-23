package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
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
public class Request implements FromBytes<Request>, IntoBytes
{
    private Head head;
    private Action action;
    private byte[] body;
    
    @Override
    public Request fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length == 0) throw new ParseException("Failed to parse bytes into Request", 0);
        if (bytes[0] != 0) throw new ParseException("Failed to check first byte", 0);

        List<byte[]> parts = FromBytes.split(Arrays.copyOfRange(bytes, 5, bytes.length), (byte) 0x1F, 2);

        if (parts.size() != 3) throw new ParseException("Failed to parse bytes into Request. Found more than 3 parts", 0);
        
        head = new Head().fromBytes(parts.get(0));
        action = new Action().fromBytes(parts.get(1));
        body = parts.get(2);
        
        return this;
    }

    @Override
    public byte[] intoBytes()
    {
        byte[] headBytes = head.intoBytes();
        byte[] actionBytes = action.intoBytes();
        byte[] bodyBytes = body;
        
        int len = headBytes.length + actionBytes.length + bodyBytes.length + 2; // 2 for the 2 0x1F separator
        
        return ByteBuffer.allocate(len + 5)
                .put((byte) 0)
                .putInt(len)
                .put(headBytes)
                .put((byte) 0x1F)
                .put(actionBytes)
                .put((byte) 0x1F)
                .put(bodyBytes)
                .array();
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "head=" + head +
                ", action=" + action +
                ", body=" + Arrays.toString(body) +
                '}';
    }
    
    public static Request creteConnectRequest(String clientName)
    {
        return new Request(
                new Head(Version.actualProtocolVersion(), clientName), 
                new Action(ActionType.CONNECT, "", ""), 
                "".getBytes(StandardCharsets.UTF_8)
        );
    }
}
