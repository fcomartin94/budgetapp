package util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for locale-aware monetary formatting.
 *
 * <p>Formats amounts as EUR currency using the {@code es-ES} locale
 * (e.g. {@code 1.234,56 \u20AC}). The non-breaking space ({@code \u00A0}) that
 * {@link NumberFormat} inserts between the amount and the currency symbol is
 * replaced with a regular space for consistent terminal output.</p>
 *
 * <p>This class is a pure utility \u2014 it holds no state and cannot be instantiated.</p>
 */
public final class MoneyFormatter {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-ES");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_ES);

    private MoneyFormatter() {
    }

    /**
     * Formats a monetary amount as EUR currency in the {@code es-ES} locale.
     *
     * @param amount the amount to format
     * @return a formatted string such as {@code "1.234,56 \u20AC"}
     */
    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount).replace('\u00A0', ' ');
    }
}
