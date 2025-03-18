package org.lebastudios.theroundtable.config;

import java.io.File;
import java.util.Locale;

public class PreferencesConfigData extends ConfigData<PreferencesConfigData>
{
    public String theme = "cupertino-light";
    public String language = Locale.getDefault().getLanguage();

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getUserDir() + "/preferences.json");
    }
}
