package core.library;

import basket.api.app.Property;
import basket.api.util.Version;
import java.util.function.Function;

public enum AppInfo implements Property {

    name(String::valueOf),
    current_version(Version::parse),
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
