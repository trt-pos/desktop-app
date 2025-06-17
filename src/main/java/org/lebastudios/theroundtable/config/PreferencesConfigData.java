package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.themes.Theme;

import java.io.File;

public class PreferencesConfigData extends ConfigData<PreferencesConfigData>
{
    public String theme = Theme.DEFAULT.url().toExternalForm();

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getUserDir() + "/preferences.json");
    }
}
