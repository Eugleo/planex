package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;

public class Config {
    final LocalDate beginning;
    final Set<ClassOptions> classOptions;
    final WeightsConfig weightsConfig;
    public final ClassOptions userDefaultOpts;

    public static final WeightsConfig defaultWeightsConfig =
            new WeightsConfig(new StatusFunction(3, 2, 1), 1, 1, 1);
    public static final ClassOptions defaultClassOptions = new ClassOptions(
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

    @SuppressWarnings("UnusedReturnValue")
    static class Builder extends OptionParser {
        private LocalDate beginning;
        ClassOptions userDefaultOpts = Config.defaultClassOptions;
        private final Set<ClassOptions> classOptions = new HashSet<>();
        private WeightsConfig weightsConfig;
        int defaultYear;

        // ADAM jak zformátovat tyhle víceřádkové function cally?
        {
            addOption("začátek", value ->
                    setBeginning(Utils.parseDate(value, defaultYear)
                            .orElseThrow(() -> new IncorrectConfigFileException("Incorrect date format"))));
        }

        Builder() { }

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
