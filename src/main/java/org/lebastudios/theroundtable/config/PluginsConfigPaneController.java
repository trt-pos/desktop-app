package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.lebastudios.theroundtable.dialogs.RequestTextDialogController;
import org.lebastudios.theroundtable.plugins.PluginRepoIntrospector;

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
        configData.repos.clear();
        configData.repos.addAll(reposList.getItems());
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
        new RequestTextDialogController(url -> reposList.getItems().add(url),
                 "https://repo.domain.com", "New plugin repository",
                url ->
                {
                    try
                    {
                        return new PluginRepoIntrospector(url).ping();
                    }
                    catch (Exception exception)
                    {
                        return false;
                    }
                }).setOwner(this.getStage())
                .instantiate();
    }

    @FXML
    public void removeRepo(ActionEvent actionEvent)
    {
        String selected = reposList.getSelectionModel().getSelectedItem();

        if (selected == null) return;
        ;

        reposList.getItems().remove(selected);
    }
}
