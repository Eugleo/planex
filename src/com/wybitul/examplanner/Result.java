package com.wybitul.examplanner;

import java.time.LocalDate;

public class Result {
    ClassOptions classOptions;
    LocalDate examDate;
    LocalDate start;

    int backupTries;
    int prepTime;

    public Result(ClassOptions classOptions, LocalDate examDate, LocalDate start, int backupTries, int prepTime) {
        this.classOptions = classOptions;
        this.examDate = examDate;
        this.start = start;
        this.backupTries = backupTries;
        this.prepTime = prepTime;
    }
}
