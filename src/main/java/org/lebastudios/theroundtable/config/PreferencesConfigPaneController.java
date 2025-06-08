package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import org.lebastudios.theroundtable.apparience.ThemeLoader;
import org.lebastudios.theroundtable.locale.Translator;

import java.util.Objects;

public class PreferencesConfigPaneController extends ConfigPaneController<PreferencesConfigData>
{
    @FXML public ChoiceBox<String> themeChoiceBox;

    public PreferencesConfigPaneController()
    {
        super(new PreferencesConfigData(), Translator.getInstance().t("core:word.preferences"), "core:preferences.png");
    }

    @Override
    public void updateConfigData(PreferencesConfigData configData)
    {
        configData.theme = transformThemeToInternalText(themeChoiceBox.getValue());
    }

    @Override
    public void updateUI(PreferencesConfigData configData)
    {
        if (themeChoiceBox.getItems().isEmpty())
        {
            var themesDir = ThemeLoader.getThemesDir();

            for (var theme : Objects.requireNonNull(themesDir.listFiles()))
            {
                if (theme.isFile()) continue;

                themeChoiceBox.getItems().add(transformThemeToDisplayableText(theme.getName()));
            }
        }

        themeChoiceBox.setValue(transformThemeToDisplayableText(configData.theme));
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }

    @Override
    public void onSave(PreferencesConfigData configData)
    {
        ThemeLoader.reloadThemes();
    }

    private String transformThemeToDisplayableText(String theme)
    {
        int index = theme.indexOf("-");
        
        if (index == -1) 
        {
            return theme.substring(0, 1).toUpperCase() + theme.substring(1);
        }
        
        return theme.substring(0, 1).toUpperCase() + theme.substring(1, index) + " " 
                + theme.substring(index + 1, index + 2).toUpperCase() + theme.substring(index + 2);
    }

    private String transformThemeToInternalText(String theme)
    {
        return theme.toLowerCase().replace(" ", "-");
    }
}
