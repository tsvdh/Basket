package core.library;

import java.util.LinkedList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.AppSession;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfflineAppInfo {

    private LinkedList<AppSession> sessions;
}
