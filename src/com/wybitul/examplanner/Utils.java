package com.wybitul.examplanner;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private Utils() { }

    public static Optional<LocalDate> parseDate(String str, int defaultYear) {
        Pattern p = Pattern.compile("(\\d{1,2})\\.\\s*(\\d{1,2})\\.(?:\\s*(\\d{4}))?");
        Matcher m = p.matcher(str);

        if (!m.find() || (m.group(3) == null && defaultYear == -1)) {
            return Optional.empty();
        }

        int day = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int year = defaultYear;
        if (m.group(3) != null) {
            year = Integer.parseInt(m.group(3));
        }

        return Optional.of(LocalDate.of(year, month, day));
    }

    public static String formatDate(LocalDate date, int defaultYear) {
        if (date.getYear() == defaultYear) {
            return String.format("%d. %d.", date.getDayOfMonth(), date.getMonthValue());
        } else {
            return String.format("%d. %d. %d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
        }
    }

    public static Optional<Boolean> parseBoolean(String s) {
        switch (s.toLowerCase()) {
            case "y":
            case "yes":
            case "true":
            case "ano":
            case "a":
                return Optional.of(true);
            case "n":
            case "no":
            case "false":
            case "ne":
                return Optional.of(false);
            default:
                return Optional.empty();
        }
    }

    public static Optional<Integer> parseInteger(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
