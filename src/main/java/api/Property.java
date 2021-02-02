package api;

import java.util.function.Function;

@FunctionalInterface
public interface Property {

    Function<String, Object> getParser();
}
