package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

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
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>(head.toBytes());
        
        bytes.add((byte) 0x1F);
        
        bytes.addAll(action.toBytes());
        bytes.add((byte) 0x1F);
        
        for (byte b : body) bytes.add(b);

        byte msgType = (byte) 0;
        int length = bytes.size();

        byte[] lengthBytes = new byte[4];

        lengthBytes[0] = (byte) (length >> 24);
        lengthBytes[1] = (byte) (length >> 16);
        lengthBytes[2] = (byte) (length >> 8);
        lengthBytes[3] = (byte) (length);
        
        bytes.addFirst(lengthBytes[3]);
        bytes.addFirst(lengthBytes[2]);
        bytes.addFirst(lengthBytes[1]);
        bytes.addFirst(lengthBytes[0]);
        bytes.addFirst(msgType);
        
        return bytes;
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
