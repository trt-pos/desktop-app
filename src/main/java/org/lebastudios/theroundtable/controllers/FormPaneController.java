package org.lebastudios.theroundtable.controllers;

public abstract class FormPaneController<T> extends Controller<FormPaneController<?>>
{
    protected T object;
    
    public void setObject(T object)
    {
        this.object = object;
        updateUI(object);
    }
    
    protected abstract void updateUI(T object);
    
    public abstract boolean validate();
    public abstract T buildObject(T object);
}
