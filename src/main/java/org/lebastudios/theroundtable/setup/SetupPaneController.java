package org.lebastudios.theroundtable.setup;

import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.plugins.IPlugin;

public abstract class SetupPaneController extends PaneController<SetupPaneController>
{
    public abstract void apply();

    public abstract boolean validate();
    
    @Override
    public final Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }
}
