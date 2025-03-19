package org.lebastudios.theroundtable;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.plugins.PluginsManager;

public class HomePaneController extends PaneController<HomePaneController>
{
    @FXML private FlowPane flowPane;

    @Override
    protected void initialize()
    {
        super.initialize();
        
        flowPane.getChildren().addAll(PluginsManager.getInstance().getHomeButtons());
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
