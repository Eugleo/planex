package com.wybitul.examplanner;

public class WordFormatter {
    String zeroAndFivePlus;
    String one;
    String twoToFive;

    public WordFormatter(String zeroAndFivePlus, String one, String twoToFive) {
        this.zeroAndFivePlus = zeroAndFivePlus;
        this.one = one;
        this.twoToFive = twoToFive;
    }

    public String format(int i) {
        if (i == 0 || i > 4) {
            return i + " " + zeroAndFivePlus;
        } else if (i == 1) {
            return i + " " + one;
        } else {
            return i + " " + twoToFive;
        }
    }
}
