package api;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {

    private static final int LENGTH = 3;

    private final String raw;
    private final int[] array;

    public Version(String version) {
        raw = version;
        String[] strings = version.split("\\.");
        if (strings.length != LENGTH) {
            throw new IllegalArgumentException("Version not of length " + LENGTH);
        }

        array = new int[3];
        for (int i = 0; i < LENGTH; i++) {
            array[i] = Integer.parseInt(strings[i]);
        }
    }

    @Override
    public String toString() {
        return raw;
    }

    @Override
    public int compareTo(@NotNull Version other) {
        for (int i = 0; i < LENGTH; i++) {
            int thisPart = this.array[i];
            int otherPart = other.array[i];

            if (thisPart != otherPart) {
                return thisPart - otherPart;
            }
        }
        return 0;
    }
}
