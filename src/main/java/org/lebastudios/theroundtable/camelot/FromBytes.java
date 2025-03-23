package org.lebastudios.theroundtable.camelot;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface FromBytes<T>
{
    T fromBytes(byte[] bytes) throws ParseException;
    
    static List<byte[]> split(byte[] bytes, byte separator, int times)
    {
        List<byte[]> bytesArraysList = new ArrayList<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        for (int i = 0; i < bytes.length; i++) 
        {
            byte actualByte = bytes[i];

            if (actualByte == separator)
            {
                bytesArraysList.add(buffer.toByteArray());
                buffer.reset();

                if (bytesArraysList.size() == times)
                {
                    bytesArraysList.add(Arrays.copyOfRange(bytes, i + 1, bytes.length));
                    break;
                }

                continue;
            }
            
            buffer.write(actualByte);
        }
        
        return bytesArraysList;
    }
}
