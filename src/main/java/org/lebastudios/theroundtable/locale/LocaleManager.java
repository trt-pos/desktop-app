package org.lebastudios.theroundtable.locale;

import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.PreferencesConfigData;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class LocaleManager
{
    private static LocaleManager instance;
    
    public static LocaleManager getInstance()
    {
        if (instance == null) instance = new LocaleManager();
        
        return instance;
    }
    
    private LocaleManager() {}
    
    public Locale getActualLocale()
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
    
    public DateTimeFormatter getActualDateTimeFormatter()
    {
        return DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(getActualLocale());
    }
    
    public Currency getActualCurrency()
    {
        return Currency.EUR;
    }
}
