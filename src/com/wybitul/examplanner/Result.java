package com.wybitul.examplanner;

public class Result {
    UniversityClass uniClass;
    Exam exam;
    long prepTime;

    public Result(UniversityClass uniClass, Exam exam, long prepTime) {
        this.uniClass = uniClass;
        this.exam = exam;
        this.prepTime = prepTime;
    }
}
