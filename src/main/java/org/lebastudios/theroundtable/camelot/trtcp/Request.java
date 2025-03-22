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

        List<byte[]> parts = FromBytes.splitBytes(Arrays.copyOfRange(bytes, 1, bytes.length), (byte) 0x1F);

        if (parts.size() != 3) throw new ParseException("Failed to parse bytes into Request", 0);
        
        head = new Head().fromBytes(parts.get(0));
        action = new Action().fromBytes(parts.get(1));
        body = parts.get(2);
        
        return this;
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>();
        
        bytes.add((byte) 0);
        
        bytes.addAll(head.toBytes());
        bytes.add((byte) 0x1F);
        
        bytes.addAll(action.toBytes());
        bytes.add((byte) 0x1F);
        
        for (byte b : body) bytes.add(b);
        
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
    
    public static Request ConnectRequest(String clientName)
    {
        return new Request(
                new Head(Version.actualProtocolVersion(), clientName), 
                new Action(ActionType.CONNECT, "", ""), 
                "".getBytes(StandardCharsets.UTF_8)
        );
    }
}
