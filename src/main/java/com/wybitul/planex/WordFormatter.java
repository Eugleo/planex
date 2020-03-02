package com.wybitul.planex;

/*
Used to format numbers (e.g. 1 -> 1 den, 9 -> 9 dní)
 */

public class WordFormatter {
    final String zeroAndFivePlus;
    final String one;
    final String twoToFive;

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
