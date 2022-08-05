package core.library;

import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistentAppInfo {

    private Duration timeUsed;
    private OffsetDateTime lastUsed;
    private boolean serverNotifiedOfAcquisition;
}
