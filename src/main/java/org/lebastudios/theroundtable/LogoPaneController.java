package org.lebastudios.theroundtable;

import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.plugins.IPlugin;

public class LogoPaneController extends PaneController<LogoPaneController>
{
    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }
}
