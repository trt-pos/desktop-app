package org.lebastudios.theroundtable.events;


import org.lebastudios.theroundtable.logs.Logs;

import java.util.function.Consumer;

public class LocalEvent<T> extends EventHandler<Consumer<T>>
{
    public void invoke(T t)
    {
        for (var listener : getActiveListeners())
        {
            try
            {
                listener.accept(t);
            }
            catch (Exception e)
            {
                Logs.getInstance().log(
                        "Error while invoking listener for event: " + this.getClass().getSimpleName(),
                        e
                );
            }
        }
    }
}
