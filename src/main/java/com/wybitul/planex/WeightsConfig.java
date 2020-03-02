package com.wybitul.planex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
Holds the specification of weight parameters, which are used to compute class importance.
 */

public class WeightsConfig {
    final StatusFunction st;
    final int s;
    final int w;
    final int c;

    public WeightsConfig(StatusFunction st, int s, int w, int c) {
        this.st = st;
        this.s = s;
        this.w = w;
        this.c = c;
    }

    @SuppressWarnings("UnusedReturnValue")
    static class Builder extends OptionParser {
        private StatusFunction st;
        private int s;
        private int w;
        private int c;

        // ADAM opravdu nebudou {w, c, s, st} použity?
        // Neměly by být nastaveny až poté, co se ta lambda zavolá? (i.e. po zavolání konstruktoru)
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

        Builder() {
            this.st = Config.defaultWeightsConfig.st;
            this.s = Config.defaultWeightsConfig.s;
            this.w = Config.defaultWeightsConfig.w;
            this.c = Config.defaultWeightsConfig.c;
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
