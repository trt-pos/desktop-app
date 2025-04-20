package org.lebastudios.theroundtable.events;

import com.google.gson.GsonBuilder;
import org.lebastudios.theroundtable.camelot.*;
import org.lebastudios.theroundtable.env.Variables;

import java.util.function.Consumer;

public class CamelotEvent<T extends IntoBytes & FromBytes<T>> extends EventHandler<Consumer<T>>
{
    private final String eventName;

    public CamelotEvent(String eventName, T obj)
    {
        this.eventName = eventName;

        CamelotEventsManager.getInstance().addListener(
                eventName,
                new NetEventHandler(obj)
        );
    }

    public void invoke(T body)
    {
        CamelotEventsManager.getInstance().invokeEvent(eventName, body);
    }

    private class NetEventHandler extends CamelotEventListener<T>
    {
        public NetEventHandler(FromBytes<T> bytesParser)
        {
            super(bytesParser);
        }

        @Override
        public void accept(T body)
        {
            
            if (Variables.isVerbose())
            {
                System.out.println("Event: " + eventName);
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(body));
            }
            
            for (var listener : getActiveListeners())
            {
                try
                {
                    listener.accept(body);
                }
                catch (Exception e)
                {
                    System.err.println("Error invoking event: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
