package util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-ES");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_ES);

    private MoneyFormatter() {
    }

    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount).replace('\u00A0', ' ');
    }
}
