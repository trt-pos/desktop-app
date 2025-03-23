package org.lebastudios.theroundtable.camelot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class FromObjectToJsonBytes implements IntoBytes
{
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Object value;
    
    public FromObjectToJsonBytes(Object value)
    {
        this.value = value;
    }
    
    @Override
    public byte[] intoBytes()
    {
        return new FromStringToBytes(GSON.toJson(value)).intoBytes();
    }
}
