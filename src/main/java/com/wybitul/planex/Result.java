package com.wybitul.planex;

import java.time.LocalDate;

public class Result {
    final ClassOptions classOptions;
    final LocalDate examDate;
    final LocalDate start;

    final int backupTries;
    final int prepTime;

    public Result(ClassOptions classOptions, LocalDate examDate, LocalDate start, int backupTries, int prepTime) {
        this.classOptions = classOptions;
        this.examDate = examDate;
        this.start = start;
        this.backupTries = backupTries;
        this.prepTime = prepTime;
    }
}
