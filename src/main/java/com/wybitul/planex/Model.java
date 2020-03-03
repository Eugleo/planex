package com.wybitul.planex;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
The workhorse of this program. Uses CP-SAT to solve the model specified by Config.
 */

public class Model {
    final CpModel model = new CpModel();
    final Set<ClassModel> classModels = new HashSet<>();

    final LocalDate beginning;

    Model(Config config) throws ModelException {
        this.beginning = config.beginning;
        List<ClassOptions> classOptions = config.classOptions.stream()
                .filter(opt -> !opt.ignore)
                .collect(Collectors.toList());

        LocalDate lastDate = classOptions.stream()
                .flatMap(c -> c.examDates.stream())
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new ModelException("Can't find last exam date. Are there any exams?"));
        int dayCount = Math.toIntExact(ChronoUnit.DAYS.between(beginning, lastDate)) + 1;
        List<IntervalVar> intervals = new ArrayList<>();
        List<Integer> importances = new ArrayList<>();
        List<IntVar> prepTimeDiffs = new ArrayList<>();
        IntVar zero = model.newConstant(0);
        IntVar one = model.newConstant(1);

        for (ClassOptions classOpts : classOptions) {
            // Each interval denotes the learning/preparation period for an exam
            // i.e. the end of each interval has to be before some exam
            int[][] endDays = getEndDays(classOpts, beginning);

            String name = classOpts.classInfo.name;

            // I take duration as the size of interval (left, right], so I have to shift the initial date to -1
            IntVar start = model.newIntVar(-1, dayCount, name + "start");
            IntVar duration = model.newIntVar(0, dayCount, name + "duration");
            IntVar end = model.newIntVar(0, dayCount, name + "end");
            IntervalVar interval = model.newIntervalVar(start, duration, end, name + "interval");

            intervals.add(interval);

            // prepTime = duration - 1
            IntVar prepTime = model.newIntVar(0, dayCount, name + "prepTime");
            model.addEquality(prepTime, LinearExpr.scalProd(new IntVar[]{duration, one}, new int[]{1, -1}));

            // prepTime >= minPrepTime
            IntVar minPrepTime = model.newConstant(classOpts.minPrepTime);
            model.addGreaterOrEqual(prepTime, minPrepTime);

            classOpts.lowBound.ifPresent(d -> model.addGreaterOrEqual(end, ChronoUnit.DAYS.between(beginning, d)));
            classOpts.highBound.ifPresent(d -> model.addLessOrEqual(end, ChronoUnit.DAYS.between(beginning, d)));

            // backupTries is the number of backup exam dates (i.e. if the exam needs to be done again)
            IntVar backupTries = model.newIntVar(0, classOpts.examDates.size(), name + "backupTries");
            model.addGreaterOrEqual(backupTries, classOpts.backupTries);

            try {
                model.addAllowedAssignments(new IntVar[]{end, backupTries}, endDays);
            } catch (CpModel.WrongLength wrongLength) {
                System.out.println("Incorrect tuple length");
            }

            // prepTimeDiff = idealPrepTime - prepTime
            IntVar idealPrepTime = model.newConstant(classOpts.idealPrepTime);
            IntVar prepTimeDiff = model.newIntVar(-dayCount, classOpts.idealPrepTime, name + "prepDiff");
            LinearExpr expr = LinearExpr.scalProd(new IntVar[]{idealPrepTime, prepTime}, new int[]{1, -1});
            model.addEquality(expr, prepTimeDiff);

            // prepTimeDiffPos = max(0, prepTimeDiff)
            IntVar prepTimeDiffPos = model.newIntVar(0, classOpts.idealPrepTime, name + "prepDiffPos");
            model.addMaxEquality(prepTimeDiffPos, new IntVar[]{prepTimeDiff, zero});

            classModels.add(new ClassModel(classOpts, start, end, prepTime, backupTries));

            importances.add(classOpts.getImportance(config.weightsConfig) + 1);
            prepTimeDiffs.add(prepTimeDiffPos);
        }

        // You can't learn for two exams simultaneously
        model.addNoOverlap(intervals.toArray(IntervalVar[]::new));

        // Minimize the following objective
        // sum over all classes C: prepTimeDiffPos(C) * (w * weight(C) + c * credits(C) + s * st(status(C)))
        model.minimize(
                LinearExpr.scalProd(
                        prepTimeDiffs.toArray(IntVar[]::new),
                        importances.stream().mapToInt(i -> i).toArray()
                )
        );
    }

    // Return a list of days which are just before an exam
    private int[][] getEndDays(ClassOptions classOptions, LocalDate firstDay) {
        Matrix<Integer> matrix = new Matrix<>();

        List<Integer> endDays = classOptions.examDates.stream()
                .map(d -> ChronoUnit.DAYS.between(firstDay, d))
                .map(Math::toIntExact)
                .collect(Collectors.toList());
        matrix.addColumn(endDays);

        List<Integer> remainingBackupTries = IntStream.range(0, endDays.size()).boxed().collect(Collectors.toList());
        Collections.reverse(remainingBackupTries);
        matrix.addColumn(remainingBackupTries);

        return matrix.toIntArray();
    }
}
