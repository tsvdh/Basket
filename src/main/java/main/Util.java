package main;

import static java.lang.Math.signum;

public class Util {

    public static boolean signEqual(Number a, Number b) {
        return signum(a.floatValue()) == signum(b.floatValue());
    }
}
