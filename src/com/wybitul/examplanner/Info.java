package com.wybitul.examplanner;

import java.util.ArrayList;
import java.util.List;

public class Info {
    String name;
    List<Exam> exams = new ArrayList<>();

    public Info(String name) {
        this.name = name;
    }
}
