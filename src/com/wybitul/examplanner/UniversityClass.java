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
    List<Exam> exams;
    ClassParams params;

    public UniversityClass(String id, ExamInfo examInfo, ClassParams cp) {
        this.id = id;
        this.name = examInfo.name;
        this.params = cp;
        Predicate<Exam> pred = cp.isColloquium ? e -> e.type == ExamType.Colloquium : e -> e.type == ExamType.Exam;
        this.exams = examInfo.exams.stream().filter(pred).collect(Collectors.toList());
    }

    public int getImportance(WeightConfigurator w) {
        return w.type * w.typeToInt.apply(params.type) + w.weight * params.weight + w.credits * params.credits;
    }
}
