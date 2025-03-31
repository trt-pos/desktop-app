package org.lebastudios.theroundtable.setup;

import org.lebastudios.theroundtable.controllers.PaneController;

public abstract class SetupPaneController extends PaneController<SetupPaneController>
{
    public abstract void apply();

    public abstract boolean validate();
}
