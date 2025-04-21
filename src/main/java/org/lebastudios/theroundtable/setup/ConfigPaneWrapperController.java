package org.lebastudios.theroundtable.setup;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.ConfigPaneController;
import org.lebastudios.theroundtable.ui.TitleBuilder;

class ConfigPaneWrapperController extends SetupPaneController
{
    private final ConfigPaneController<?> configPaneController;
    
    @SneakyThrows
    public ConfigPaneWrapperController(ConfigPaneController<?> configPaneController)
    {
        this.configPaneController = configPaneController;
    }

    @Override
    @FXML
    protected void initialize()
    {
        ((BorderPane) getRoot()).setCenter(configPaneController.getRoot());
        ((BorderPane) getRoot()).setTop(
                new TitleBuilder(
                        configPaneController.getController().getTitle(),
                        configPaneController.getController().getIconName()
                ).build()
        );
    }

    @Override
    public void apply()
    {
        configPaneController.getController().apply();
    }

    @Override
    public boolean validate()
    {
        return configPaneController.getController().validate().success();
    }
}
