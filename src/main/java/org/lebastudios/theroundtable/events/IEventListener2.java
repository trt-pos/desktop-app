package org.lebastudios.theroundtable.events;

public interface IEventListener2<T, P>
{
    void invoke(T t, P p);
}
