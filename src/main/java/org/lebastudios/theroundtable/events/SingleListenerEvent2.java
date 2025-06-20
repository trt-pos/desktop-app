package org.lebastudios.theroundtable.events;

import lombok.NonNull;

public class SingleListenerEvent2<T, P> extends Event2<T, P>
{
    private boolean hasListener = false;

    @Override
    public void addListener(@NonNull IEventListener2<T, P> listener)
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
    public void removeListener(@NonNull IEventListener2<T, P> listener)
    {
        if (!getActiveListeners().contains(listener))
        {
            return;
        }

        super.removeListener(listener);
        hasListener = false;
    }
}
