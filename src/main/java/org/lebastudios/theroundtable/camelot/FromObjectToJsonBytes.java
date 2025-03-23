package org.lebastudios.theroundtable.camelot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public final class FromObjectToJsonBytes implements IntoBytes
{
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Object value;
    
    public FromObjectToJsonBytes(Object value)
    {
        this.value = value;
    }
    
    @Override
    public List<Byte> toBytes()
    {
        return new FromStringToBytes(GSON.toJson(value)).toBytes();
    }
}
