package org.lebastudios.theroundtable.camelot.converters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lebastudios.theroundtable.camelot.FromBytes;
import org.lebastudios.theroundtable.camelot.FromBytesToString;
import org.lebastudios.theroundtable.camelot.FromStringToBytes;
import org.lebastudios.theroundtable.camelot.IntoBytes;

import java.text.ParseException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StringConverter implements IntoBytes, FromBytes<StringConverter>
{
    private String string;
    
    @Override
    public StringConverter fromBytes(byte[] bytes) throws ParseException
    {
        return new StringConverter(new FromBytesToString().fromBytes(bytes));
    }

    @Override
    public byte[] intoBytes()
    {
        return new FromStringToBytes(string).intoBytes();
    }
}
