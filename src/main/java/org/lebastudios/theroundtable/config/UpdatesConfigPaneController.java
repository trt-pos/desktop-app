package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.lebastudios.theroundtable.locale.LangFileLoader;

public class UpdatesConfigPaneController extends ConfigPaneController<UpdatesConfigData>
{
    @FXML public CheckBox disableUpdatesCheckBox;

    public UpdatesConfigPaneController()
    {
        super(
                new UpdatesConfigData(),
                LangFileLoader.getTranslation("word.updates"), 
                "reload.png"
        );
    }

    @Override
    public void updateConfigData(UpdatesConfigData configData)
    {
        configData.checkUpdates = disableUpdatesCheckBox.isSelected();
    }

    @Override
    public void updateUI(UpdatesConfigData configData)
    {
        disableUpdatesCheckBox.setSelected(configData.checkUpdates);
    }

    @Override
    public boolean validate()
    {
        return true;
    }
}
