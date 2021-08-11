package main;

import app.Property;

import java.util.function.Function;

public enum Settings implements Property {

    name(o -> o),
    age(Integer::parseInt),
    height(Double::parseDouble);

    private final Function<String, Object> parser;

    Settings(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
