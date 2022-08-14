package server.common.model.user;

import java.util.HashMap;
import lombok.Data;

@Data
public class User {

    private String id;

    private String email;

    private String username;

    private String encodedPassword;

    private HashMap<String, AppUsage> usageInfo;

    private boolean developer;

    private DeveloperInfo developerInfo;
}
