package org.lebastudios.theroundtable.camelot;

import org.lebastudios.theroundtable.camelot.trtcp.Head;
import org.lebastudios.theroundtable.camelot.trtcp.Request;
import org.lebastudios.theroundtable.camelot.trtcp.Version;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.ArrayList;
import java.util.HashMap;
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

    private final HashMap<String, List<CamelotEventListener<?>>> events = new HashMap<>();

    final Consumer<Request> callbacksHandler = request ->
    {
        String event = request.getAction().getEvent();

        if (!events.containsKey(event))
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Received callback for event " + event + " but no listeners are registered"
            );
            return;
        }
        events.get(event).forEach(listener -> listener.accept(request.getBody()));
    };

    private CamelotEventsManager() {}

    public void removeListener(String event, CamelotEventListener<?> listener)
    {
        if (!events.containsKey(event)) return;

        events.get(event).remove(listener);
        
        if (events.get(event).isEmpty())
        {
            CamelotServiceManager.getInstance().getPersistentClient().removeListener(event);
            events.remove(event);
        }
    }

    public void addListener(String event, CamelotEventListener<?> listener)
    {
        if (!events.containsKey(event))
        {
            createEvent(event);
        }

        events.get(event).add(listener);
    }

    private void createEvent(String event)
    {
        CamelotClient client = CamelotServiceManager.getInstance().getPersistentClient();
        
        client.createEvent(event);

        events.put(event, new ArrayList<>());
    }

    public void invokeEvent(String event, IntoBytes intoBytes)
    {
        CamelotClient client = CamelotServiceManager.getInstance().getPersistentClient();
        
        client.invokeEvent(event, intoBytes);
    }
}
