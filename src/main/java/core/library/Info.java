package core.library;

import app.Property;
import java.util.function.Function;
import util.Version;

public enum Info implements Property {

    name(String::valueOf),
    current_version(Version::of),
    use_latest(Boolean::parseBoolean);

    private final Function<String, Object> parser;

    Info(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
