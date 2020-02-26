package com.wybitul.examplanner;

import java.util.ArrayList;
import java.util.List;

public class ExamInfo {
    String name;
    List<Exam> exams = new ArrayList<>();

    public ExamInfo(String name) {
        this.name = name;
    }
}
