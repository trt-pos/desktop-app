package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.logs.Logs;

import java.text.ParseException;

public abstract class CamelotEventListener<T>
{
    private final FromBytes<T> bytesParser;
    
    public CamelotEventListener(FromBytes<T> bytesParser) 
    {
        this.bytesParser = bytesParser;
    }
    
    final void accept(byte[] bytes)
    {
        try
        {
            accept(bytesParser.fromBytes(bytes));
        }
        catch (ParseException e)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "Camelot Event Listener failed to parse bytes"
            );
        }
    }
    
    public abstract void accept(T body);
}
