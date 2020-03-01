package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
All information about a class. Mostly used in model building.
 */

@SuppressWarnings({"UnusedReturnValue", "OptionalUsedAsFieldOrParameterType"})
public class ClassOptions {
    final ClassInfo classInfo;
    final Status status;
    final int idealPrepTime;
    final int minPrepTime;
    final int weight;
    final int credits;
    final int backupTries;
    final Optional<LocalDate> lowBound;
    final Optional<LocalDate> highBound;
    final Set<LocalDate> examDates;

    final boolean ignore;

    ClassOptions(ClassInfo classInfo, Status status, int idealPrepTime, int minPrepTime, int weight, int credits,
                 int backupTries, boolean ignore, Optional<LocalDate> lowBound, Optional<LocalDate> highBound,
                 Set<LocalDate> examDates) {
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

    static class Builder extends OptionParser {
        ClassInfo classInfo;
        private Status status;
        private int idealPrepTime;
        private int minPrepTime;
        private int weight;
        int credits;
        int defaultYear;
        private int backupTries;
        private Optional<LocalDate> lowBound;
        private Optional<LocalDate> highBound;
        private boolean ignore = false;
        private Set<LocalDate> examDates;

        // ADAM Rád bych místo Stringů použil nějaký enum, kde by byly uloženy všechny možné optiony
        // Jak na to?
        {
            addOption("rozmezí", value -> {
                Pattern generalP = Pattern.compile("^\\s*(.+)\\s*-\\s*(.+)\\s*$");
                Matcher generalM = generalP.matcher(value);

                if (!generalM.find()) { throw new IncorrectConfigFileException("Incorrect date range format"); }

                setLowBound(Utils.parseDate(generalM.group(1), defaultYear));
                setHighBound(Utils.parseDate(generalM.group(2), defaultYear));
            });

            addOption("optimum", value -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                if (!m.find()) { throw new IncorrectConfigFileException("Incorrect number format"); }
                idealPrepTime = Integer.parseInt(m.group(1));
            });

            addOption("kredity", value -> credits = Integer.parseInt(value));

            addOption("váha", value -> weight = Integer.parseInt(value));

            addOption("status", value -> {
                switch (value.toLowerCase()) {
                    case "povinný":
                    case "povinné":
                    case "p":
                        status = Status.P;
                        break;
                    case "povinně volitelný":
                    case "povinně volitelné":
                    case "pvp":
                        status = Status.PVP;
                        break;
                    case "volitelný":
                    case "volitelné":
                    case "v":
                        status = Status.V;
                        break;
                    default:
                        throw new IncorrectConfigFileException("Incorrect status format");
                }
            });

            addFlag("ignorovat", () -> ignore = true);

            addFlag("zápočet", () -> classInfo.type = Type.COLLOQUIUM);

            addOption("minimum", value -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                if (!m.find()) { throw new IncorrectConfigFileException("Incorrect number format"); }
                setMinPrepTime(Integer.parseInt(m.group(1)));
            });

            addOption("pokusy", value -> backupTries = Integer.parseInt(value));

            addOption("termíny", value -> {
                String[] dateStrings = value.split(",\\s*");
                examDates = Arrays.stream(dateStrings)
                        .map(str -> Utils.parseDate(str, defaultYear))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
            });
        }

        Builder(ClassInfo classInfo, ClassOptions defaultClassOpts, int defaultYear) {
            cloneFields(defaultClassOpts);
            this.classInfo = classInfo;
            this.defaultYear = defaultYear;
        }

        Builder(ClassOptions defaultClassOpts, int defaultYear) {
            cloneFields(defaultClassOpts);
            this.defaultYear = defaultYear;
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

        public Builder setLowBound(Optional<LocalDate> lowBound) {
            this.lowBound = lowBound;
            return this;
        }

        public Builder setHighBound(Optional<LocalDate> highBound) {
            this.highBound = highBound;
            return this;
        }

        public Builder setExamDates(Set<LocalDate> examDates) {
            this.examDates = examDates;
            return this;
        }

        public Builder setIgnore(boolean ignore) {
            this.ignore = ignore;
            return this;
        }

        public ClassOptions createClassOptions() {
            return new ClassOptions(classInfo, status, idealPrepTime, minPrepTime, weight,
                    credits, backupTries, ignore, lowBound, highBound, examDates);
        }
    }
}