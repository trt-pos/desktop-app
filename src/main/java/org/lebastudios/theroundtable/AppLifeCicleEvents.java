package org.lebastudios.theroundtable;

import javafx.stage.WindowEvent;
import org.lebastudios.theroundtable.events.LocalEvent;

public final class AppLifeCicleEvents
{
    public static final LocalEvent<WindowEvent> onAppCloseRequest = new LocalEvent<>();
    public static final LocalEvent<Void> onAppShutdown = new LocalEvent<>();
}
