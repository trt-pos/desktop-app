package org.lebastudios.theroundtable.camelot.trtcp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface FromBytes<T>
{
    T fromBytes(byte[] bytes) throws ParseException;
    
    static List<byte[]> splitBytes(byte[] bytes, byte separator)
    {
        List<byte[]> bytesArraysList = new ArrayList<>();
        List<Byte> buffer = new ArrayList<>();
        
        for (var actualByte : bytes)
        {
            if (actualByte == separator) 
            {
                byte[] bufferArray = new byte[buffer.size()];
                
                for (int i = 0; i < buffer.size(); i++) bufferArray[i] = buffer.get(i);
                
                bytesArraysList.add(bufferArray);
                buffer.clear();
                continue;
            }
            
            buffer.add(actualByte);
        }
        
        return bytesArraysList;
    }
}
