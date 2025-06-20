package org.lebastudios.theroundtable.plugins;

import org.lebastudios.theroundtable.events.LocalEvent;

public final class PluginEvents
{
    private PluginEvents() {}

    public static final LocalEvent<IPlugin> onPluginLoaded = new LocalEvent<>();
    public static final LocalEvent<IPlugin> onPluginUnloaded = new LocalEvent<>();
}
