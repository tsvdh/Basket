package server.common.model.user;

import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class AppUsage {

    private Duration timeUsed;

    private OffsetDateTime lastUsed;
}
