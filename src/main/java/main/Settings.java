package main;

import java.time.OffsetDateTime;
import java.util.HashSet;
import jfxtras.styles.jmetro.Style;
import lombok.Data;

@Data
public class Settings {

    private Style jmetroStyle;
    private HashSet<String> acquiredApps;
    private OffsetDateTime currentReleaseDate;
}
