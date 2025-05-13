package org.lebastudios.theroundtable.config;

import java.io.File;

public class PreferencesConfigData extends ConfigData<PreferencesConfigData>
{
    public String theme = "cupertino-light";

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getUserDir() + "/preferences.json");
    }
}
