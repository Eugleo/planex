package com.wybitul.planex.config;

import com.wybitul.planex.config.loading.OptionParser;
import com.wybitul.planex.utilities.Functions;
import com.wybitul.planex.utilities.IncorrectConfigFileException;

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
    public final ClassInfo classInfo;
    public final Status status;
    public final int idealPrepTime;
    public final int minPrepTime;
    public final int weight;
    public final int credits;
    public final int backupTries;
    public final Optional<LocalDate> lowBound;
    public final Optional<LocalDate> highBound;
    public final Set<LocalDate> examDates;

    public final boolean ignore;

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

    public static class Builder extends OptionParser {
        public ClassInfo classInfo;
        public int credits;
        public int defaultYear;
        private Status status;
        private int idealPrepTime;
        private int minPrepTime;
        private int weight;
        private int backupTries;
        private Optional<LocalDate> lowBound;
        private Optional<LocalDate> highBound;
        private boolean ignore = false;
        private Set<LocalDate> examDates;

        {
            addOption("rozmezí", value -> {
                Pattern generalP = Pattern.compile("^\\s*(.+)\\s*-\\s*(.+)\\s*$");
                Matcher generalM = generalP.matcher(value);

                if (!generalM.find()) {
                    throw new IncorrectConfigFileException("Incorrect date range format");
                }

                setLowBound(Functions.parseDate(generalM.group(1), defaultYear));
                setHighBound(Functions.parseDate(generalM.group(2), defaultYear));
            });

            addOption("optimum", value -> {
                Pattern p = Pattern.compile("^(\\d+)");
                Matcher m = p.matcher(value);
                if (!m.find()) {
                    throw new IncorrectConfigFileException("Incorrect number format");
                }
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
                if (!m.find()) {
                    throw new IncorrectConfigFileException("Incorrect number format");
                }
                setMinPrepTime(Integer.parseInt(m.group(1)));
            });

            addOption("pokusy", value -> backupTries = Integer.parseInt(value));

            addOption("termíny", value -> {
                String[] dateStrings = value.split(",\\s*");
                examDates = Arrays.stream(dateStrings)
                        .map(str -> Functions.parseDate(str, defaultYear))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
            });
        }

        public Builder(ClassInfo classInfo, ClassOptions defaultClassOpts, int defaultYear) {
            cloneFields(defaultClassOpts);
            this.classInfo = classInfo;
            this.defaultYear = defaultYear;
        }

        public Builder(ClassOptions defaultClassOpts, int defaultYear) {
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