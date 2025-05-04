package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class PluginsConfigPaneController extends ConfigPaneController<PluginsConfigData>
{
    @FXML public ListView<String> reposList;

    public PluginsConfigPaneController()
    {
        super(new PluginsConfigData(), "Plugins", "plugins.png");
    }

    @Override
    public void updateConfigData(PluginsConfigData configData)
    {
        
    }

    @Override
    public void updateUI(PluginsConfigData configData)
    {
        reposList.getItems().setAll(configData.repos);
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }

    @FXML
    public void addRepo(ActionEvent actionEvent)
    {
        
    }

    @FXML
    public void removeRepo(ActionEvent actionEvent)
    {
        
    }
}
