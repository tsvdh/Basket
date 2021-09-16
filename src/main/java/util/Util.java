package util;

import basket.api.common.PathHandler;
import java.nio.file.Path;

import static java.lang.Math.signum;

public class Util {

    public static boolean signEqual(Number a, Number b) {
        return signum(a.floatValue()) == signum(b.floatValue());
    }

    public static Path getAppLibraryPath(String appName) {
        return PathHandler.getBasketHomePath().resolve("apps/library").resolve(appName);
    }
}
