package com.wybitul.planex.solver;

import com.google.ortools.sat.IntVar;
import com.wybitul.planex.config.ClassOptions;

public class ClassModel {
    public final ClassOptions classOptions;
    public final IntVar start;
    public final IntVar end;
    public final IntVar preparationTime;
    public final IntVar backupTries;

    public ClassModel(ClassOptions classOptions, IntVar start, IntVar end, IntVar preparationTime, IntVar backupTries) {
        this.classOptions = classOptions;
        this.start = start;
        this.end = end;
        this.preparationTime = preparationTime;
        this.backupTries = backupTries;
    }
}
