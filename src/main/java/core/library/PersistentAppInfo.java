package core.library;

import basket.api.app.Property;
import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Function;

public enum PersistentAppInfo implements Property {

    time_used(Duration::parse),
    last_used(LocalDate::parse);

    private final Function<String, Object> parser;

    PersistentAppInfo(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
