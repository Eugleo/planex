package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    LocalDate beginning;
    Set<ClassOptions> classOptions;
    WeightsConfig weightsConfig;
    public ClassOptions userDefaultOpts;

    public static WeightsConfig defaultWeightsConfig =
            new WeightsConfig(new StatusFunction(3, 2, 1), 1, 1, 1);
    public static ClassOptions defaultClassOptions = new ClassOptions(
            null, Status.V, 0, 0, 1, 0, 0,
            false, null, null, new HashSet<>()
    );

    public Config(LocalDate beginning, ClassOptions userDefaultOpts,
                  Set<ClassOptions> classOptions, WeightsConfig weightsConfig) {
        this.beginning = beginning;
        this.userDefaultOpts = userDefaultOpts;
        this.classOptions = classOptions;
        this.weightsConfig = weightsConfig;
    }

    static class Builder extends OptionParser {
        private LocalDate beginning;
        ClassOptions userDefaultOpts = Config.defaultClassOptions;
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
                beginning = LocalDate.of(year, month, day);
            });
        }

        public Builder addClassOptions(ClassOptions opt) {
            classOptions.add(opt);
            return this;
        }

        public Builder setBeginning(LocalDate beginning) {
            this.beginning = beginning;
            return this;
        }

        public Builder setWeightsConfig(WeightsConfig weightsConfig) {
            this.weightsConfig = weightsConfig;
            return this;
        }

        public Builder setUserDefaultOpts(ClassOptions classOptions) {
            this.userDefaultOpts = classOptions;
            return this;
        }

        public Config createConfig() {
            return new Config(beginning, userDefaultOpts, classOptions, weightsConfig);
        }
    }
}
