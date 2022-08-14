package server.common.model.app;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class Release {

    public enum Type {
        STABLE, EXPERIMENTAL
    }

    private String version;

    private OffsetDateTime date;

    private Type type;
}
