package com.wybitul.planex.config;

import com.wybitul.planex.config.loading.OptionParser;
import com.wybitul.planex.utilities.Functions;
import com.wybitul.planex.utilities.IncorrectConfigFileException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/*
Holds everything the model needs to do its job. Closely reflects the configuration file.
 */

public class Config {
    public static final WeightsConfig defaultWeightsConfig =
            new WeightsConfig(new StatusFunction(3, 2, 1), 1, 1, 1);
    public static final ClassOptions defaultClassOptions = new ClassOptions(
            null, Status.P, 0, 0, 3, 0, 0,
            false, Optional.empty(), Optional.empty(), new HashSet<>()
    );
    public final ClassOptions globalClassOptions;
    public final LocalDate beginning;
    public final Set<ClassOptions> classOptions;
    public final WeightsConfig weightsConfig;

    public Config(LocalDate beginning, ClassOptions globalClassOptions,
                  Set<ClassOptions> classOptions, WeightsConfig weightsConfig) {
        this.beginning = beginning;
        this.globalClassOptions = globalClassOptions;
        this.classOptions = classOptions;
        this.weightsConfig = weightsConfig;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder extends OptionParser {
        public LocalDate beginning;
        public ClassOptions globalClassOptions;
        public Set<ClassOptions> classOptions;
        public int defaultYear;
        private WeightsConfig weightsConfig;

        {
            addOption("začátek", value ->
                    setBeginning(Functions.parseDate(value, defaultYear)
                            .orElseThrow(() -> new IncorrectConfigFileException("Incorrect date format"))));
        }

        public Builder() {
            classOptions = new HashSet<>();
            globalClassOptions = Config.defaultClassOptions;
            weightsConfig = Config.defaultWeightsConfig;
        }

        public Builder(Config defaultConfig) {
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
