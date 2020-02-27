package com.wybitul.examplanner;

public class WordFormatter {
    String w04;
    String w1;
    String wRest;

    public WordFormatter(String w04, String w1, String wRest) {
        this.w04 = w04;
        this.w1 = w1;
        this.wRest = wRest;
    }

    public String format(int i) {
        if (i == 0 || i > 4) {
            return w04;
        } else if (i == 1) {
            return w1;
        } else {
            return wRest;
        }
    }
}
