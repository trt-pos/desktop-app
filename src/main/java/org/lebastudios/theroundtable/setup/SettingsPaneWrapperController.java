package org.lebastudios.theroundtable.setup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.ConfigPaneController;

public class SettingsPaneWrapperController extends SetupPaneController
{
    private final ConfigPaneController<?> configPaneController;
    
    @SneakyThrows
    public SettingsPaneWrapperController(ConfigPaneController<?> configPaneController, Node titleNode)
    {
        super(titleNode);
        this.configPaneController = configPaneController;
    }

    @Override
    @FXML
    protected void initialize()
    {
        ((BorderPane) getRoot()).setCenter(configPaneController.getRoot());
        ((BorderPane) getRoot()).setTop(titleNode);
    }

    @Override
    public void apply()
    {
        configPaneController.getController().apply();
    }

    @Override
    public boolean validate()
    {
        return configPaneController.getController().validate();
    }
}
