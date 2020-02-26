package com.wybitul.examplanner;

import java.time.LocalDate;

public class Config {
    ClassType type;
    int idealPrepTime = -1;
    int weight = -1;
    int credits = -1;
    LocalDate lowBound;
    LocalDate highBound;

    public Config() { }
}
