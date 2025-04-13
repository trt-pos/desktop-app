package org.lebastudios.theroundtable.locale;

public record Currency(char symbol, String abbreviation)
{
    public static final Currency USD = new Currency('$', "USD");
    public static final Currency EUR = new Currency('€', "EUR");
    public static final Currency GBP = new Currency('£', "GBP");
    
    public static Currency[] availableCurrencies()
    {
        return new Currency[] { USD, EUR, GBP };
    }
}
