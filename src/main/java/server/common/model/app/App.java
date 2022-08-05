package server.common.model.app;

import java.util.Set;
import lombok.Data;

@Data
public class App {
    
    private String id;

    private String name;

    private String description;

    private String admin;

    private Set<String> developers;

    private AppStats appStats;

    private boolean available;

    private Release stable;

    private Release experimental;
}
