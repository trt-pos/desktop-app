package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

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

        List<byte[]> parts = FromBytes.split(Arrays.copyOfRange(bytes, 5, bytes.length), (byte) 0x1F, 2);

        if (parts.size() != 3) throw new ParseException("Failed to parse bytes into Response. Found more than 3 parts", 0);

        head = new Head().fromBytes(parts.get(0));
        statusCode = StatusCode.values()[0].fromBytes(parts.get(1));
        body = parts.get(2);

        return this;
    }

    @Override
    public List<Byte> toBytes()
    {
        List<Byte> bytes = new ArrayList<>(head.toBytes());
        
        bytes.add((byte) 0x1F);
        
        bytes.addAll(statusCode.toBytes());
        bytes.add((byte) 0x1F);
        
        for (byte b : body) bytes.add(b);

        byte msgType = (byte) 1;
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
        return "Response{" +
                "head=" + head +
                ", statusCode=" + statusCode +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
