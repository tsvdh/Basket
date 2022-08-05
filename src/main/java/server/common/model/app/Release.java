package server.common.model.app;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Release {

    public enum Type {
        STABLE, EXPERIMENTAL
    }

    private String version;

    private LocalDate date;

    private Type type;
}
