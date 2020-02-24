package com.wybitul.examplanner;

import java.util.function.Function;

public class WeightConfigurator {
    Function<ClassType, Integer> typeToInt;
    int type;
    int weight;
    int credits;

    public WeightConfigurator(Function<ClassType, Integer> typeToInt, int type, int weight, int credits) {
        this.typeToInt = typeToInt;
        this.type = type;
        this.weight = weight;
        this.credits = credits;
    }
}
