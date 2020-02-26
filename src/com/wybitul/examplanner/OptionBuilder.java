package com.wybitul.examplanner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionBuilder<T extends HasValidation> {
    private Map<String, BiConsumer<String, T>> actions = new HashMap<>();
    private Map<String, Consumer<T>> flags = new HashMap<>();
    protected T object;

    public OptionBuilder(T defaultObject) {
        this.object = defaultObject;
    }

    public void addOption(String option, BiConsumer<String, T> f) {
        actions.put(option, f);
    }

    public void addFlag(String flag, Consumer<T> f) { flags.put(flag, f); }

    public void parse(String line) throws IncorrectConfigFileException {
        try {
            Pattern p = Pattern.compile("^-\\s+(.*)\\s*:\\s+(.+)\\s*$");
            Matcher m = p.matcher(line);

            if (m.find()) {
                String name = m.group(1);
                String value = m.group(2);

                actions.get(name).accept(value, object);
            } else {
                Pattern p2 = Pattern.compile("^-\\s+(.*)\\s*$");
                Matcher m2 = p2.matcher(line);
                m2.find();

                String name = m2.group(1);

                flags.get(name).accept(object);
            }
        } catch (Exception e) {
            throw new IncorrectConfigFileException();
        }
    }

    public T build() throws MissingFieldException {
        object.validate();
        return object;
    }
}
