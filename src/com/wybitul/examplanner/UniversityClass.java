package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.List;

enum ClassType {
    P, PVP, V
}

public class UniversityClass {
    String name;
    String id;
    ClassType type;
    int idealPrepTime;
    int weight;
    int credits;
    List<Exam> exams;
    LocalDate lowBound;
    LocalDate highBound;

    public UniversityClass(String id, Info info, Config config) {
        this.id = id;
        this.name = info.name;
        this.type = config.type;
        this.idealPrepTime = config.idealPrepTime;
        this.weight = config.weight;
        this.credits = config.credits;
        this.exams = info.exams;
        this.lowBound = config.lowBound;
        this.highBound = config.highBound;
    }

    public int getImportance(WeightConfigurator w) {
        return w.type * w.typeToInt.apply(type) + w.weight * weight + w.credits * credits;
    }
}
