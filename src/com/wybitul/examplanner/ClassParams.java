package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassParams implements HasValidation {
    ClassType type = ClassType.V;
    int idealPrepTime = 0;
    int weight = 0;
    int credits = 0;
    LocalDate lowBound;
    LocalDate highBound;

    public ClassParams() { }

    // Cloning constructor
    public ClassParams(ClassParams clone) {
        switch (clone.type) {
            case V:
                type = ClassType.V;
                break;
            case P:
                type = ClassType.P;
                break;
            case PVP:
                type = ClassType.PVP;
                break;
        }
        idealPrepTime = clone.idealPrepTime;
        weight = clone.weight;
        credits = clone.credits;
        lowBound = clone.lowBound == null ? null : LocalDate.from(clone.lowBound);
        highBound = clone.highBound == null ? null : LocalDate.from(clone.highBound);
    }

    @Override
    public void validate() throws MissingFieldException {
        if (type == null) {
            throw new MissingFieldException("Missing the status field for a class. Set with \"- status: (P|PVP|V)\".");
        }
        if (idealPrepTime < 0) {
            throw new MissingFieldException("Missing the ideal preparation time field for a class. Set with \"- příprava: # (dní)?\".");
        }
        if (weight < 0) {
            throw new MissingFieldException("Missing the weight field for a class. Set with \"- váha: #\".");
        }
        if (credits < 0) {
            throw new MissingFieldException("Missing the credits field for a class. Set with \"- kredity: #\".");
        }
    }

    static class Builder extends OptionBuilder<ClassParams> {
        Builder(ClassParams defaultClassParams) {
            super(defaultClassParams);

            addOption("omezení", (value, cp) -> {
                String date = "((\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})|.)";
                Pattern p = Pattern.compile(date + "\\s*-\\s*" + date);
                Matcher m = p.matcher(value);
                m.find();
                if (!m.group(1).matches("^.$")) {
                    String days = m.group(2);
                    int day = Integer.parseInt(m.group(2));
                    int month = Integer.parseInt(m.group(3));
                    int year = Integer.parseInt(m.group(4));
                    cp.lowBound = LocalDate.of(year, month, day);
                } else if (!m.group(5).matches("^.$")) {
                    int day = Integer.parseInt(m.group(6));
                    int month = Integer.parseInt(m.group(7));
                    int year = Integer.parseInt(m.group(8));
                    cp.highBound = LocalDate.of(year, month, day);
                }
            });

            addOption("příprava", (value, cp) -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                m.find();
                cp.idealPrepTime = Integer.parseInt(m.group(1));
            });

            addOption("kredity", (value, cp) -> cp.credits = Integer.parseInt(value));
            addOption("váha", (value, cp) -> cp.weight = Integer.parseInt(value));

            addOption("status", (value, cp) -> {
                switch (value) {
                    case "P":
                        cp.type = ClassType.P;
                        break;
                    case "PVP":
                        cp.type = ClassType.PVP;
                        break;
                    case "V":
                        cp.type = ClassType.V;
                        break;
                }
            });
        }
    }
}
