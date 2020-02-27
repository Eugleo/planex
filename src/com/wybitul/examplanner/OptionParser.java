package com.wybitul.examplanner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface OptionParser {
    Map<String, Consumer<String>> optConsumers = new HashMap<>();
    Map<String, Runnable> flagActions = new HashMap<>();

    default void addOption(String option, Consumer<String> f) {
        optConsumers.put(option, f);
    }

    default void addFlag(String flag, Runnable f) {
        flagActions.put(flag, f);
        optConsumers.put(flag, s -> {
            switch (s.toLowerCase()) {
                case "true":
                    f.run();
                    break;
                case "false":
                    break;
            }
        });
    }

    default void parse(String line) throws IncorrectConfigFileException {
        try {
            Pattern p = Pattern.compile("^-\\s+(.*)\\s*:\\s+(.+)\\s*$");
            Matcher m = p.matcher(line);

            if (m.find()) {
                String name = m.group(1);
                String value = m.group(2);

                optConsumers.get(name).accept(value);
            } else {
                Pattern p2 = Pattern.compile("^-\\s+(.*)\\s*$");
                Matcher m2 = p2.matcher(line);
                m2.find();

                String name = m2.group(1);

                flagActions.get(name).run();
            }
        } catch (Exception e) {
            throw new IncorrectConfigFileException();
        }
    }
}
