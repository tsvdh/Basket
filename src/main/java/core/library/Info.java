package core.library;

import app.Property;
import java.util.function.Function;
import util.Version;

public enum Info implements Property {

    name(String::valueOf),
    description(String::valueOf),
    current_version(Version::valueOf),
    stable_version(Version::valueOf),
    latest_version(Version::valueOf);

    private final Function<String, Object> parser;

    Info(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
