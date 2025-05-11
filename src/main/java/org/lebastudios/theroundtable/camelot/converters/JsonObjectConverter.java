package org.lebastudios.theroundtable.camelot.converters;

import lombok.Getter;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.FromJsonBytesToObject;
import org.lebastudios.theroundtable.camelot.FromObjectToJsonBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.text.ParseException;

public class JsonObjectConverter<T> implements IntoBytes, FromBytes<JsonObjectConverter<T>>
{
    private final Class<T> clazz;
    @Getter private T object;
    
    public JsonObjectConverter(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    public JsonObjectConverter(T object)
    {
        this.object = object;
        this.clazz = (Class<T>) object.getClass();
    }
    
    @Override
    public JsonObjectConverter<T> fromBytes(byte[] bytes) throws ParseException
    {
        return new JsonObjectConverter<>(new FromJsonBytesToObject<>(clazz).fromBytes(bytes));
    }

    @Override
    public byte[] intoBytes()
    {
        return new FromObjectToJsonBytes(object).intoBytes();
    }
}
