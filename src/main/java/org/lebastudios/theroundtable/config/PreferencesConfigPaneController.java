package org.lebastudios.theroundtable.config;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.themes.Theme;

public class PreferencesConfigPaneController extends ConfigPaneController<PreferencesConfigData>
{
    @FXML public ChoiceBox<Theme> themeChoiceBox;

    public PreferencesConfigPaneController()
    {
        super(new PreferencesConfigData(), Translator.getInstance().t("core:word.preferences"), "core:preferences.png");
    }

    @Override
    protected void initialize()
    {
        themeChoiceBox.setConverter(Theme.STRING_CONVERTER);
        
        super.initialize();
    }

    @Override
    public void updateConfigData(PreferencesConfigData configData)
    {
        configData.theme = themeChoiceBox.getValue().url().toExternalForm();
    }

    @Override
    public void updateUI(PreferencesConfigData configData)
    {
        if (themeChoiceBox.getItems().isEmpty())
        {
            PluginsManager.getInstance()
                    .getLoadedPlugins()
                    .forEach(p -> themeChoiceBox.getItems().addAll(p.getStyles()));
        }

        themeChoiceBox.getSelectionModel().select(0);
    }

    @Override
    public void onSave(PreferencesConfigData configData)
    {
        Application.setUserAgentStylesheet(configData.theme);
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }
}
