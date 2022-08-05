package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.lang.Math.pow;
import static java.lang.Math.signum;

public class Util {

    public static boolean signEqual(Number a, Number b) {
        return signum(a.floatValue()) == signum(b.floatValue());
    }

    public static String toMB(long bytes) {
        double MB = pow(10, 6);

        DecimalFormat decimalFormat = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
        double megaBytes = bytes / MB;

        return decimalFormat.format(megaBytes);
    }
}
