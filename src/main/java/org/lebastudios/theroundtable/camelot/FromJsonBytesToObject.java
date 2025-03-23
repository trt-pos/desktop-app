package org.lebastudios.theroundtable.camelot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;

public final class FromJsonBytesToObject<T> implements FromBytes<T>
{
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Class<T> clazz;
    
    public FromJsonBytesToObject(Class<T> clazz)
    {
        this.clazz = clazz;
    }
    
    @Override
    public T fromBytes(byte[] bytes) throws ParseException
    {
        return GSON.fromJson(new FromBytesToString().fromBytes(bytes), clazz);
    }
}
