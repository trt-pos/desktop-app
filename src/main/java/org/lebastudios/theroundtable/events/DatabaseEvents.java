package org.lebastudios.theroundtable.events;

public class DatabaseEvents
{
    public static final LocalEvent<Void> onDatabaseInit = new LocalEvent<>();
    public static final LocalEvent<Void> onDatabaseClose = new LocalEvent<>();
}
