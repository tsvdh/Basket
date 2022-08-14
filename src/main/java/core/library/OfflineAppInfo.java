package core.library;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfflineAppInfo {

    @Data
    @AllArgsConstructor
    public static class Session {
        private OffsetDateTime start;
        private OffsetDateTime end;
    }

    private LinkedList<Session> sessions;
}
