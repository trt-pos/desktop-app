package org.lebastudios.theroundtable.locale;

import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.GlobalPreferencesConfigData;

import java.time.format.DateTimeFormatter;
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

        Language language = new GlobalPreferencesConfigData().load().language;

        return Locale.of(language.language(), language.country());
    }

    public DateTimeFormatter getActualDateTimeFormatter()
    {
        return DateTimeFormatter.ofPattern(
            new GlobalPreferencesConfigData().load().dateTimeFormatter
        );
    }

    public Currency getActualCurrency()
    {
        return new GlobalPreferencesConfigData().load().currency;
    }
}
