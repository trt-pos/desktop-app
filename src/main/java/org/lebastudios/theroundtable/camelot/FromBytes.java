package org.lebastudios.theroundtable.camelot;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface FromBytes<T>
{
    T fromBytes(byte[] bytes) throws ParseException;
    
    static List<byte[]> splitBytes(byte[] bytes, byte separator)
    {
        List<byte[]> bytesArraysList = new ArrayList<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        for (var actualByte : bytes)
        {
            if (actualByte == separator) 
            {
                bytesArraysList.add(buffer.toByteArray());
                buffer.reset();
                continue;
            }
            
            buffer.write(actualByte);
        }
        
        bytesArraysList.add(buffer.toByteArray());
        
        return bytesArraysList;
    }
}
