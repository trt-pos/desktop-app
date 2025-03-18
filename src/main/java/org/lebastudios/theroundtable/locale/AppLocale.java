package org.lebastudios.theroundtable.locale;

import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.files.JsonFile;
import org.lebastudios.theroundtable.config.PreferencesConfigData;

import java.util.Locale;

public class AppLocale
{
    public static Locale getActualLocale()
    {
        if (AccountManager.getInstance().getCurrentLogged() == null) 
        {
            return Locale.getDefault();
        }
        
        return Locale.of(
                new PreferencesConfigData().load().language,
                System.getProperty("user.country")
        );
    }
}
