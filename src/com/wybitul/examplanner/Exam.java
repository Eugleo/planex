package com.wybitul.examplanner;

import java.time.LocalDate;

enum ExamType {
    Exam, Colloquium
}

public class Exam {
    LocalDate date;
    ExamType type;

    public Exam(LocalDate date, ExamType type) {
        this.date = date;
        this.type = type;
    }
}
