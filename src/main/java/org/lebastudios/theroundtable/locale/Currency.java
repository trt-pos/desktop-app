package org.lebastudios.theroundtable.locale;

import javafx.util.StringConverter;

import java.util.Locale;

public record Currency(char symbol, String abbreviation)
{
    public static final StringConverter<Currency> CONVERTER = new StringConverter<Currency>() {

        @Override
        public String toString(Currency object)
        {
            return object.abbreviation + " (" + object.symbol + ")";
        }

        @Override
        public Currency fromString(String string)
        {
            for (Currency currency : availableCurrencies())
            {
                if (CONVERTER.toString(currency).equals(string))
                {
                    return currency;
                }
            }
            return USD;
        }
    };
    
    public static final Currency USD = new Currency('$', "USD");
    public static final Currency EUR = new Currency('€', "EUR");
    public static final Currency GBP = new Currency('£', "GBP");

    public static Currency[] availableCurrencies()
    {
        return new Currency[] { USD, EUR, GBP };
    }

    public static Currency getDefault() 
    {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        
        return switch (country)
        {
            case "GB" -> GBP;
            case "ES" -> EUR;
            default -> USD;
        };
    }
}
