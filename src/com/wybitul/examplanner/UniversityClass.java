package com.wybitul.examplanner;

import java.util.List;
import java.util.function.Function;

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

    public UniversityClass(String id, ClassConfig c, List<Exam> exams) {
        this.name = c.name;
        this.id = id;
        this.type = c.type;
        this.idealPrepTime = c.idealPrepTime;
        this.weight = c.weight;
        this.credits = c.credits;
        this.exams = exams;
    }

    public int getImportance(WeightConfigurator w) {
        return w.type * w.typeToInt.apply(type) + w.weight * weight + w.credits * credits;
    }
}
