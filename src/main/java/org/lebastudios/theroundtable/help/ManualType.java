package org.lebastudios.theroundtable.help;

import org.lebastudios.theroundtable.locale.LangFileLoader;

public enum ManualType
{
    USER, TECH;
    
    public String getResourceFolderName()
    {
        return switch (this)
        {
            case USER -> "user-manual";
            case TECH -> "tech-manual";
        };
    }
    
    public String getManualName()
    {
        return switch (this)
        {
            case USER -> LangFileLoader.getTranslation("manual.user");
            case TECH -> LangFileLoader.getTranslation("manual.tech");
        };
    }
    
    public String getIconName()
    {
        return switch (this)
        {
            case USER -> "user.png";
            case TECH -> "tech.png";
        };
    }
}
