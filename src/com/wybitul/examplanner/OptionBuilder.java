package com.wybitul.examplanner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionBuilder<T extends HasValidation> {
    private Map<String, BiConsumer<String, T>> actions = new HashMap<>();
    protected T object;

    public OptionBuilder(T defaultObject) {
        this.object = defaultObject;
    }

    public void addOption(String option, BiConsumer<String, T> f) {
        actions.put(option, f);
    }

    public void parse(String line) throws IncorrectConfigFileException {
        try {
            Pattern p = Pattern.compile("^-\\s+(.*)\\s*:\\s+(.+)\\s*$");
            Matcher m = p.matcher(line);
            m.find();

            String name = m.group(1);
            String value = m.group(2);

            actions.get(name).accept(value, object);
        } catch (Exception e) {
            throw new IncorrectConfigFileException();
        }
    }

    public T build() throws MissingFieldException {
        object.validate();
        return object;
    }
}
