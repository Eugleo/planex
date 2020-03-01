package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;

/*
Holds everything the model needs to do its job. Closely reflects the configuration file.
 */

public class Config {
    final LocalDate beginning;
    final Set<ClassOptions> classOptions;
    final WeightsConfig weightsConfig;
    public final ClassOptions globalClassOptions;

    public static final WeightsConfig defaultWeightsConfig =
            new WeightsConfig(new StatusFunction(3, 2, 1), 1, 1, 1);
    public static final ClassOptions defaultClassOptions = new ClassOptions(
            null, Status.P, 0, 0, 3, 0, 0,
            false, Optional.empty(), Optional.empty(), new HashSet<>()
    );

    public Config(LocalDate beginning, ClassOptions globalClassOptions,
                  Set<ClassOptions> classOptions, WeightsConfig weightsConfig) {
        this.beginning = beginning;
        this.globalClassOptions = globalClassOptions;
        this.classOptions = classOptions;
        this.weightsConfig = weightsConfig;
    }

    @SuppressWarnings("UnusedReturnValue")
    static class Builder extends OptionParser {
        LocalDate beginning;
        ClassOptions globalClassOptions;
        Set<ClassOptions> classOptions;
        private WeightsConfig weightsConfig;
        int defaultYear;

        // ADAM jak zformátovat tyhle víceřádkové function cally?
        {
            addOption("začátek", value ->
                    setBeginning(Utils.parseDate(value, defaultYear)
                            .orElseThrow(() -> new IncorrectConfigFileException("Incorrect date format"))));
        }

        Builder() {
            classOptions = new HashSet<>();
            globalClassOptions = Config.defaultClassOptions;
            weightsConfig = Config.defaultWeightsConfig;
        }

        Builder(Config defaultConfig) {
            beginning = defaultConfig.beginning;
            globalClassOptions = defaultConfig.globalClassOptions;
            classOptions = defaultConfig.classOptions;
            weightsConfig = defaultConfig.weightsConfig;
        }

        public Builder setClassOptions(Set<ClassOptions> classOptions) {
            this.classOptions = classOptions;
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

        public Builder setGlobalClassOptions(ClassOptions classOptions) {
            this.globalClassOptions = classOptions;
            return this;
        }

        public Config createConfig() {
            return new Config(beginning, globalClassOptions, classOptions, weightsConfig);
        }
    }
}
