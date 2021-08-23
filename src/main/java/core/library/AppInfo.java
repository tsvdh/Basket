package core.library;

import app.Property;
import java.util.function.Function;
import util.Version;

public enum AppInfo implements Property {

    name(String::valueOf),
    current_version(Version::of),
    use_experimental(Boolean::parseBoolean);

    private final Function<String, Object> parser;

    AppInfo(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
