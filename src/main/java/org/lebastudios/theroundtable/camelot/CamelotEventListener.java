package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.logs.Logs;

import java.text.ParseException;

public abstract class CamelotEventListener<T extends FromBytes<T>>
{
    private final T auxObj;
    
    private CamelotEventListener(T auxObj) 
    {
        this.auxObj = auxObj;
    }
    
    final void accept(byte[] bytes)
    {
        try
        {
            accept(auxObj.fromBytes(bytes));
        }
        catch (ParseException e)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "Camelot Event Listener failed to parse bytes"
            );
        }
    }
    
    public abstract void accept(T object);
}
