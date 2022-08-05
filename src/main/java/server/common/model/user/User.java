package server.common.model.user;

import java.util.HashSet;
import lombok.Data;

@Data
public class User {

    private String id;

    private String email;

    private String username;

    private String encodedPassword;

    private HashSet<String> userOf;

    private boolean developer;

    private DeveloperInfo developerInfo;
}
