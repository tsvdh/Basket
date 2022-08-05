package server.common.model.app;

import java.util.Map;
import lombok.Data;

@Data
public class Rating {

    private Float grade;

    private Map<String, Integer> reviews;
}
