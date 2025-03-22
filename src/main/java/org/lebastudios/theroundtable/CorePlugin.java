package org.lebastudios.theroundtable;

import org.lebastudios.theroundtable.plugins.IPlugin;

public class CorePlugin implements IPlugin
{
    private static CorePlugin instance;
    
    public static CorePlugin getInstance()
    {
        if (instance == null) instance = new CorePlugin();
        
        return instance;
    }
    
    private CorePlugin() {}
    
    @Override
    public void initialize()
    {
        
    }
}
