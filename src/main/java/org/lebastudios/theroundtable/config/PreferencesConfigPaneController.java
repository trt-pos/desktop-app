package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.ThemeLoader;

import java.util.Objects;

public class PreferencesConfigPaneController extends ConfigPaneController<PreferencesConfigData>
{
    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private ChoiceBox<String> languageChoiceBox;

    public PreferencesConfigPaneController()
    {
        super(new PreferencesConfigData());
    }

    @Override
    public void updateConfigData(PreferencesConfigData configData)
    {
        configData.language = transformLanguageToInternalText(languageChoiceBox.getValue());
        configData.theme = transformThemeToInternalText(themeChoiceBox.getValue());
        ThemeLoader.reloadThemes();
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

        if (languageChoiceBox.getItems().isEmpty())
        {
            languageChoiceBox.getItems().addAll(
                    transformLanguageToDisplayableText("es"),
                    transformLanguageToDisplayableText("en")
            );
        }

        themeChoiceBox.setValue(transformThemeToDisplayableText(configData.theme));
        languageChoiceBox.setValue(transformLanguageToDisplayableText(configData.language));
    }

    @Override
    public boolean validate()
    {
        return true;
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

    private String transformLanguageToDisplayableText(String language)
    {
        return switch (language)
        {
            case "es" -> "Español";
            case "en" -> "English";
            default -> language;
        };
    }

    private String transformLanguageToInternalText(String language)
    {
        return switch (language)
        {
            case "Español" -> "es";
            case "English" -> "en";
            default -> language;
        };
    }
    
    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

}
