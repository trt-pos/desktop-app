package org.lebastudios.theroundtable.camelot.trtcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.nio.ByteBuffer;
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
    public byte[] intoBytes()
    {
        byte[] headBytes = head.intoBytes();
        byte[] statusCodeBytes = statusCode.intoBytes();
        byte[] bodyBytes = body;

        int len = headBytes.length + statusCodeBytes.length + bodyBytes.length + 2; // 2 for the 2 0x1F separator

        return ByteBuffer.allocate(len + 5)
                .put((byte) 0)
                .putInt(len)
                .put(headBytes)
                .put((byte) 0x1F)
                .put(statusCodeBytes)
                .put((byte) 0x1F)
                .put(bodyBytes)
                .array();
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
