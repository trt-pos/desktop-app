package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.camelot.trtcp.Request;
import org.lebastudios.theroundtable.logs.Logs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CamelotEventsManager
{
    private static CamelotEventsManager instance;

    public static CamelotEventsManager getInstance()
    {
        if (instance == null) instance = new CamelotEventsManager();

        return instance;
    }

    private final HashMap<String, List<CamelotEventListener<?>>> eventListeners = new HashMap<>();
    private final HashMap<String, List<WeakReference<CamelotEventListener<?>>>> weakEventListeners = new HashMap<>();

    final Consumer<Request> callbacksHandler = request ->
    {
        String event = request.getAction().getEvent();

        if (eventListeners.containsKey(event))
        {
            eventListeners.get(event).forEach(listener -> listener.accept(request.getBody()));
        }
        
        if (weakEventListeners.containsKey(event))
        {
            // Remove listeners that have been garbage collected and notify the rest
            Iterator<WeakReference<CamelotEventListener<?>>> iterator = weakEventListeners.get(event).iterator();
            while (iterator.hasNext())
            {
                WeakReference<CamelotEventListener<?>> wr = iterator.next();
                CamelotEventListener<?> listener = wr.get();

                if (listener == null)
                {
                    iterator.remove();
                    continue;
                }

                listener.accept(request.getBody());
            }
        }

        if (!eventListeners.containsKey(event) && !weakEventListeners.containsKey(event))
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "The event " + event + " has received a callback but this manager doesn't have record of it."
            );
            CamelotServiceManager.getInstance().getPersistentClient().removeListener(event);
        }
    };

    private CamelotEventsManager() {}

    public void removeListener(String event, CamelotEventListener<?> listener)
    {
        if (!eventListeners.containsKey(event)) return;

        eventListeners.get(event).remove(listener);
    }

    public void removeWeakListener(String event, CamelotEventListener<?> listener)
    {
        if (!weakEventListeners.containsKey(event)) return;

        weakEventListeners.get(event).removeIf(wr ->
        {
            CamelotEventListener<?> l = wr.get();
            return l == null || l == listener;
        });
    }
    
    public void addListener(String event, CamelotEventListener<?> listener)
    {
        if (!eventListeners.containsKey(event))
        {
            createEvent(event);
        }

        eventListeners.get(event).add(listener);
    }
    
    public void addWeakListener(String event, CamelotEventListener<?> listener)
    {
        if (!weakEventListeners.containsKey(event))
        {
            createEvent(event);
        }

        weakEventListeners.get(event).add(new WeakReference<>(listener));
    }
    
    private void createEvent(String event)
    {
        CamelotClient client = CamelotServiceManager.getInstance().getPersistentClient();
        
        client.createAndListenEvent(event);

        eventListeners.put(event, new ArrayList<>());
        weakEventListeners.put(event, new ArrayList<>());
    }

    public void invokeEvent(String event, IntoBytes intoBytes)
    {
        CamelotClient client = CamelotServiceManager.getInstance().getPersistentClient();
        
        client.invokeEvent(event, intoBytes);
    }
    
    void updateServerEvents(CamelotClient client)
    {
        eventListeners.keySet().forEach(client::createAndListenEvent);
    }
}
