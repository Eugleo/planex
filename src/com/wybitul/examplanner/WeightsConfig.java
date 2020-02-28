package com.wybitul.examplanner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WeightsConfig {
    StatusFunction st;
    int s;
    int w;
    int c;

    public WeightsConfig(StatusFunction st, int s, int w, int c) {
        this.st = st;
        this.s = s;
        this.w = w;
        this.c = c;
    }

    static class Builder extends OptionParser {
        private StatusFunction st;
        private int s;
        private int w;
        private int c;

        {
            addOption("v", value -> w = Integer.parseInt(value));

            addOption("k", value -> c = Integer.parseInt(value));

            addOption("s", value -> s = Integer.parseInt(value));

            addOption("st", value -> {
                List<Integer> values = Arrays.stream(value.split(",\\s*"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                st = new StatusFunction(values.get(0), values.get(1), values.get(2));
            });
        }

        Builder() { }

        Builder(WeightsConfig defaultConfig) {
            this.st = defaultConfig.st;
            this.s = defaultConfig.s;
            this.w = defaultConfig.w;
            this.c = defaultConfig.c;
        }

        public Builder setStatusFunction(StatusFunction st) {
            this.st = st;
            return this;
        }

        public Builder setStatus(int s) {
            this.s = s;
            return this;
        }

        public Builder setWeight(int w) {
            this.w = w;
            return this;
        }

        public Builder setCredit(int c) {
            this.c = c;
            return this;
        }

        public WeightsConfig createWeightsConfig()
        {
            return new WeightsConfig(st, s, w, c);
        }
    }
}
