package main;

import java.time.OffsetDateTime;
import jfxtras.styles.jmetro.Style;
import lombok.Data;

@Data
public class Settings {

    private Style jmetroStyle;
    private OffsetDateTime currentReleaseDate;
}
