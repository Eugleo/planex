package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

enum ClassType {
    P, PVP, V
}

public class UniversityClass {
    String name;
    String id;
    ClassType type;
    int idealPrepTime;
    int minPrepTime;
    int weight;
    int credits;
    List<Exam> exams;
    LocalDate lowBound;
    LocalDate highBound;

    public UniversityClass(String id, ExamInfo examInfo, ClassParams cp) {
        this.id = id;
        this.name = examInfo.name;
        this.type = cp.type;
        this.idealPrepTime = cp.idealPrepTime;
        this.weight = cp.weight;
        this.credits = cp.credits;
        Predicate<Exam> pred = cp.isColloquium ? e -> e.type == ExamType.Colloquium : e -> e.type == ExamType.Exam;
        this.exams = examInfo.exams.stream().filter(pred).collect(Collectors.toList());
        this.lowBound = cp.lowBound;
        this.highBound = cp.highBound;
        this.minPrepTime = cp.minPrepTime;
    }

    public int getImportance(WeightConfigurator w) {
        return w.type * w.typeToInt.apply(type) + w.weight * weight + w.credits * credits;
    }
}
