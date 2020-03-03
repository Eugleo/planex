package com.wybitul.planex.solver;

import com.wybitul.planex.config.ClassOptions;

import java.time.LocalDate;

public class Result {
    public final ClassOptions classOptions;
    public final LocalDate examDate;
    public final LocalDate start;

    public final int backupTries;
    public final int prepTime;

    public Result(ClassOptions classOptions, LocalDate examDate, LocalDate start, int backupTries, int prepTime) {
        this.classOptions = classOptions;
        this.examDate = examDate;
        this.start = start;
        this.backupTries = backupTries;
        this.prepTime = prepTime;
    }
}
