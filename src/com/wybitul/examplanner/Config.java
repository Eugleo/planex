package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    LocalDate beginning;
    Set<ClassOptions> classOptions;
    WeightsConfig weightsConfig;

    public static WeightsConfig defaultWeightsConfig =
            new WeightsConfig(new StatusFunction(3, 2, 1), 1, 1, 1);
    public static ClassOptions defaultClassOptions =
            new ClassOptions(
                null, Status.V, 0, 0, 1, 0, 0,
                false, null, null, new ArrayList<>()
            );

    public Config(LocalDate beginning, Set<ClassOptions> classOptions, WeightsConfig weightsConfig) {
        this.beginning = beginning;
        this.classOptions = classOptions;
        this.weightsConfig = weightsConfig;
    }

    static class Builder implements OptionParser {
        private LocalDate firstDate;
        private Set<ClassOptions> classOptions = new HashSet<>();
        private WeightsConfig weightsConfig;

        {
            addOption("začátek", value -> {
                Pattern p = Pattern.compile("^(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})");
                Matcher m = p.matcher(value);
                m.find();

                int day = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                firstDate = LocalDate.of(year, month, day);
            });
        }

        public Builder addClassOptions(ClassOptions opt) {
            classOptions.add(opt);
            return this;
        }

        public Builder setFirstDate(LocalDate firstDate) {
            this.firstDate = firstDate;
            return this;
        }

        public Builder setWeightsConfig(WeightsConfig weightsConfig) {
            this.weightsConfig = weightsConfig;
            return this;
        }

        public Config createConfig() {
            return new Config(firstDate, classOptions, weightsConfig);
        }
    }
}
