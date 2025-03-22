package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Response implements FromBytes<Response>, IntoBytes
{
    private Head head;
    private StatusCode statusCode;
    private byte[] body;
    
    @Override
    public Response fromBytes(byte[] bytes) throws ParseException
    {
        if (bytes.length == 0) throw new ParseException("Failed to parse bytes into Response", 0);
        if (bytes[0] != 1) throw new ParseException("Failed to check first byte", 0);

        List<byte[]> parts = FromBytes.splitBytes(Arrays.copyOfRange(bytes, 1, bytes.length), (byte) 0x1F);

        if (parts.size() != 3) throw new ParseException("Failed to parse bytes into Response", 0);

        head = new Head().fromBytes(parts.get(0));
        statusCode = StatusCode.values()[0].fromBytes(parts.get(1));
        body = parts.get(2);

        return this;
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>();
        
        bytes.add((byte) 1);
        
        bytes.addAll(head.toBytes());
        bytes.add((byte) 0x1F);
        
        bytes.addAll(statusCode.toBytes());
        bytes.add((byte) 0x1F);
        
        for (byte b : body) bytes.add(b);
        
        return bytes;
    }

    @Override
    public String toString()
    {
        return "Response{" +
                "head=" + head +
                ", statusCode=" + statusCode +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
