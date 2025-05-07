package org.lebastudios.theroundtable.locale;

import javafx.util.StringConverter;

import java.util.Locale;

public record Language(String language, String country, String displayName)
{
    public static final StringConverter<Language> CONVERTER = new StringConverter<Language>() {

        @Override
        public String toString(Language object)
        {
            return object.displayName;
        }

        @Override
        public Language fromString(String string)
        {
            for (Language language : availableLanguages())
            {
                if (language.displayName.equals(string))
                {
                    return language;
                }
            }
            return ENGLISH;
        }
    };
    
    public static final Language ENGLISH = new Language("en", "US", "English");
    public static final Language SPANISH = new Language("es", "ES", "Español");

    public static Language[] availableLanguages()
    {
        return new Language[] { ENGLISH, SPANISH};
    }

    public static Language getDefault()
    {
        Locale locale = Locale.getDefault();
        
        for (Language language : availableLanguages())
        {
            if (language.language.equals(locale.getLanguage()) && language.country.equals(locale.getCountry()))
            {
                return language;
            }
        }
        
        return ENGLISH;
    }
}
