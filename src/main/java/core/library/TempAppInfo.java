package core.library;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.model.app.Release;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempAppInfo {

    private boolean installed;
    private Release currentRelease;
}
