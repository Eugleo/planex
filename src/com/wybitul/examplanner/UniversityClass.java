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

    public UniversityClass(String id, ExamInfo examInfo, ClassParams classParams) {
        this.id = id;
        this.name = examInfo.name;
        this.type = classParams.type;
        this.idealPrepTime = classParams.idealPrepTime;
        this.weight = classParams.weight;
        this.credits = classParams.credits;
        this.exams = examInfo.exams;
        this.lowBound = classParams.lowBound;
        this.highBound = classParams.highBound;
    }

    public int getImportance(WeightConfigurator w) {
        return w.type * w.typeToInt.apply(type) + w.weight * weight + w.credits * credits;
    }
}
