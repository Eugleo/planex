package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum Status {
    P, PVP, V
}

public class ClassOptions {
    ClassInfo classInfo;

    Status status;
    int idealPrepTime;
    int minPrepTime;
    int weight;
    int credits;
    int backupTries;

    LocalDate lowBound;
    LocalDate highBound;

    List<LocalDate> examDates;

    boolean ignore;

    ClassOptions(ClassInfo classInfo, Status status, int idealPrepTime, int minPrepTime, int weight, int credits,
                 int backupTries, boolean ignore, LocalDate lowBound, LocalDate highBound,
                 List<LocalDate> examDates) {
        this.classInfo = classInfo;
        this.status = status;
        this.idealPrepTime = idealPrepTime;
        this.minPrepTime = minPrepTime;
        this.weight = weight;
        this.credits = credits;
        this.backupTries = backupTries;
        this.ignore = ignore;
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.examDates = examDates;
    }

    public int getImportance(WeightsConfig w) {
        return w.s * w.st.apply(status) + w.w * weight + w.c * credits;
    }

    static class Builder implements OptionParser {
        ClassInfo classInfo;
        private Status status;
        private int idealPrepTime;
        private int minPrepTime;
        private int weight;
        int credits;
        private int backupTries;
        private LocalDate lowBound;
        private LocalDate highBound;
        private boolean ignore = false;
        private List<LocalDate> examDates;

        // TODO Refactor parseDate
        {
            addOption("omezení", value -> {
                String date = "((\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})|.)";
                Pattern p = Pattern.compile(date + "\\s*-\\s*" + date);
                Matcher m = p.matcher(value);
                m.find();
                if (!m.group(1).matches("^.$")) {
                    int day = Integer.parseInt(m.group(2));
                    int month = Integer.parseInt(m.group(3));
                    int year = Integer.parseInt(m.group(4));
                    lowBound = LocalDate.of(year, month, day);
                } else if (!m.group(5).matches("^.$")) {
                    int day = Integer.parseInt(m.group(6));
                    int month = Integer.parseInt(m.group(7));
                    int year = Integer.parseInt(m.group(8));
                    highBound = LocalDate.of(year, month, day);
                }
            });

            addOption("příprava", value -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                m.find();
                idealPrepTime = Integer.parseInt(m.group(1));
            });

            addOption("kredity", value -> credits = Integer.parseInt(value));

            addOption("váha", value -> weight = Integer.parseInt(value));

            addOption("status", value -> {
                switch (value) {
                    case "P":
                        status = Status.P;
                        break;
                    case "PVP":
                        status = Status.PVP;
                        break;
                    case "V":
                        status = Status.V;
                        break;
                }
            });

            addFlag("ignorovat", () -> ignore = true);

            addFlag("zápočet", () -> classInfo.type = Type.COLLOQUIUM);

            addOption("minimum", value -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                m.find();
                minPrepTime = Integer.parseInt(m.group(1));
            });

            addOption("termíny", value -> backupTries = Integer.parseInt(value));

            addOption("zkoušky", value -> {
                String[] dateStrs = value.split(",\\s*");
                examDates = Arrays.stream(dateStrs)
                        .map(ClassOptions.Builder::parseDate)
                        .collect(Collectors.toList());
            });
        }

        private static LocalDate parseDate(String str) {
            String date = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})";
            Pattern p = Pattern.compile(date);
            Matcher m = p.matcher(str);
            m.find();
            int day = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            return LocalDate.of(year, month, day);
        }

        Builder(ClassInfo classInfo) {
            this.classInfo = classInfo;
        }

        Builder(ClassInfo classInfo, ClassOptions defaultClassOpts) {
            cloneFields(defaultClassOpts);
            classInfo = classInfo;
        }

        Builder(ClassOptions defaultClassOpts) {
            cloneFields(defaultClassOpts);
        }

        private void cloneFields(ClassOptions defaultClassOpts) {
            classInfo = defaultClassOpts.classInfo;
            status = defaultClassOpts.status;
            idealPrepTime = defaultClassOpts.idealPrepTime;
            minPrepTime = defaultClassOpts.minPrepTime;
            weight = defaultClassOpts.weight;
            credits = defaultClassOpts.credits;
            backupTries = defaultClassOpts.backupTries;
            lowBound = defaultClassOpts.lowBound;
            highBound = defaultClassOpts.highBound;
            ignore = defaultClassOpts.ignore;
            examDates = defaultClassOpts.examDates;
        }

        public Builder setClass(ClassInfo classInfo) {
            this.classInfo = classInfo;
            return this;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder setIdealPrepTime(int idealPrepTime) {
            this.idealPrepTime = idealPrepTime;
            return this;
        }

        public Builder setMinPrepTime(int minPrepTime) {
            this.minPrepTime = minPrepTime;
            return this;
        }

        public Builder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder setCredits(int credits) {
            this.credits = credits;
            return this;
        }

        public Builder setBackupTries(int backupTries) {
            this.backupTries = backupTries;
            return this;
        }

        public Builder setLowBound(LocalDate lowBound) {
            this.lowBound = lowBound;
            return this;
        }

        public Builder setHighBound(LocalDate highBound) {
            this.highBound = highBound;
            return this;
        }

        public Builder setExamDates(List<LocalDate> examDates) {
            this.examDates = examDates;
            return this;
        }

        public ClassOptions createClassOptions() {
            return new ClassOptions(classInfo, status, idealPrepTime, minPrepTime, weight,
                    credits, backupTries, ignore, lowBound, highBound, examDates);
        }
    }
}