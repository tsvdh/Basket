package main;

import app.Property;

import core.StringQueue;
import java.util.function.Function;
import jfxtras.styles.jmetro.Style;

public enum Settings implements Property {

    jmetro_style(Style::valueOf),
    installed_apps(StringQueue::parse);

    private final Function<String, Object> parser;

    Settings(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }
}
