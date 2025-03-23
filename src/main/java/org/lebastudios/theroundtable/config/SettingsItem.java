package org.lebastudios.theroundtable.config;

public record SettingsItem(String value, String iconName, ConfigPaneController<?> settingPane) 
{
    public SettingsItem(String value, String iconName)
    {
        this(value, iconName, null);
    }
    
    public SettingsItem(ConfigPaneController<?> settingPane)
    {
        this(settingPane.getTitle(), settingPane.getIconName(), settingPane);
    }
}
