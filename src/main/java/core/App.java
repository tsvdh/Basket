package core;

import basket.api.util.Version;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import lombok.Data;

import static basket.api.util.Version.parse;

@Data
public class App {

    private String id;
    private String name;
    private String description;
    private Version stable;
    private Version experimental;
    private URL iconAddress;
    private URL githubHome;

    @JsonCreator
    public App(@JsonProperty("id") String id,
               @JsonProperty("name") String name,
               @JsonProperty("description") String description,
               @JsonProperty("stable") String stable,
               @JsonProperty("experimental") String experimental,
               @JsonProperty("iconAddress") URL iconAddress,
               @JsonProperty("githubHome") URL githubHome) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stable = parse(stable);
        this.experimental = parse(experimental);
        this.iconAddress = iconAddress;
        this.githubHome = githubHome;
    }
}
