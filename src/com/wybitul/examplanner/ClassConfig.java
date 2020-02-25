package com.wybitul.examplanner;

public class ClassConfig {
    String name;
    ClassType type;
    int idealPrepTime;
    int weight;
    int credits;

    public ClassConfig(String name, ClassType type, int idealPrepTime, int weight, int credits) {
        this.name = name;
        this.type = type;
        this.idealPrepTime = idealPrepTime;
        this.weight = weight;
        this.credits = credits;
    }
}
