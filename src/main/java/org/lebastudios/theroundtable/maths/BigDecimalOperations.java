package org.lebastudios.theroundtable.maths;

import org.lebastudios.theroundtable.locale.LocaleManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalOperations
{
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor)
    {
        return dividend.divide(divisor, 2, RoundingMode.CEILING);
    }

    public static BigDecimal dividePrecise(BigDecimal dividend, BigDecimal divisor)
    {
        return dividend.divide(divisor, 10, RoundingMode.FLOOR);
    }

    public static String toString(BigDecimal value)
    {
        return round(value).toString();
    }

    public static String toCurrencyString(BigDecimal value)
    {
        return toString(value) + " " + LocaleManager.getInstance().getActualCurrency().symbol();
    }

    public static BigDecimal round(BigDecimal value)
    {
        return value.setScale(2, RoundingMode.CEILING);
    }
}
