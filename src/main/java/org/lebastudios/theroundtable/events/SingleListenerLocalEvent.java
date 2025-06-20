package org.lebastudios.theroundtable.events;

import lombok.NonNull;

import java.util.function.Consumer;

public class SingleListenerLocalEvent<T> extends LocalEvent<T>
{
    private boolean hasListener = false;

    @Override
    public synchronized void addListener(@NonNull Consumer<T> listener)
    {
        if (hasListener)
        {
            System.err.println("This SingleListenerEvent already has an assigned listener");
            return;
        }

        hasListener = true;

        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(@NonNull Consumer<T> listener)
    {
        if (!getActiveListeners().contains(listener))
        {
            return;
        }

        super.removeListener(listener);
        hasListener = false;
    }
}
