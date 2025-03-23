package org.lebastudios.theroundtable.camelot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public abstract class FromJsonToObject<T extends FromJsonToObject<T>> implements FromBytes<T>
{
    private static final Gson GSON = new GsonBuilder().create();
    
    @Override
    public T fromBytes(byte[] bytes) throws ParseException
    {
        return (T) GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), this.getClass());
    }
}
