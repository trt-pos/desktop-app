package org.lebastudios.theroundtable.camelot;

import com.google.gson.GsonBuilder;
import org.lebastudios.theroundtable.env.Variables;
import org.lebastudios.theroundtable.events.EventHandler;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.util.HashMap;
import java.util.function.Consumer;

public class CamelotEvent<T extends IntoBytes & FromBytes<T>> extends EventHandler<Consumer<T>>
{
    private static final HashMap<Class<? extends IPlugin>, String> plugins = new HashMap<>();
    private final String eventName;

    public CamelotEvent(Class<? extends IPlugin> plugin, String eventName, T obj)
    {
        String pluginId = plugins.get(plugin);
        if (pluginId == null) 
        {
            pluginId = PluginsManager.getInstance().getPluginOf(plugin).orElseThrow().getPluginData().id;
            synchronized (plugins) {
                plugins.put(plugin, pluginId);
            }
        }
        
        this.eventName = pluginId + ":" + eventName;

        Logs.getInstance().log(
                Logs.LogType.INFO,
                "Registering Camelot event: " + this.eventName
        );
        
        CamelotEventsManager.getInstance().addListener(
                this.eventName,
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
