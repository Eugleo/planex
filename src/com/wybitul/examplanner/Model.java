package com.wybitul.examplanner;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Model {
    CpModel model = new CpModel();
    List<UniversityClass> classes;
    List<IntVar> starts = new ArrayList<>();
    List<IntVar> ends = new ArrayList<>();
    List<IntVar> durations = new ArrayList<>();

    LocalDate firstDate;

    // TODO Change exception to a better one
    Model(List<UniversityClass> classes, LocalDate fd, WeightConfigurator w) throws Exception {
        this.classes = classes;
        this.firstDate = fd;

        LocalDate lastDate = getDates(classes).max(Comparator.naturalOrder()).orElseThrow(() -> new Exception());
        int dayCount = Math.toIntExact(ChronoUnit.DAYS.between(firstDate, lastDate)) + 1;
        List<IntervalVar> intervals = new ArrayList<>();
        List<Integer> lossesCoefs = new ArrayList<>();
        List<IntVar> lossesVars = new ArrayList<>();
        IntVar zero = model.newConstant(0);
        IntVar one = model.newConstant(1);

        for (UniversityClass uniClass: classes) {
            int[][] endDays = getEndDays(uniClass, firstDate);

            // I take duration as the size of interval (left, right], so I have to shift the initial date
            IntVar start = model.newIntVar(-1, dayCount, uniClass.name + "start");
            IntVar duration = model.newIntVar(0, dayCount, uniClass.name + "duration");
            IntVar end = model.newIntVar(0, dayCount, uniClass.name + "end");
            IntervalVar interval = model.newIntervalVar(start, duration, end, uniClass.name + "interval");

            if (uniClass.lowBound != null) {
                long day = ChronoUnit.DAYS.between(firstDate, uniClass.lowBound);
                model.addGreaterOrEqual(end, day);
            }

            if (uniClass.highBound != null) {
                long day = ChronoUnit.DAYS.between(firstDate, uniClass.highBound);
                model.addLessOrEqual(end, day);
            }

            starts.add(start);
            ends.add(end);
            durations.add(duration);
            intervals.add(interval);

            try {
                model.addAllowedAssignments(new IntVar[] {end}, endDays);
            } catch (CpModel.WrongLength wrongLength) {
                System.out.println("Incorrect tuple length");
            }

            // prepTimeDiff = idealPrepTime - duration + 1
            IntVar idealPrepTime = model.newConstant(uniClass.idealPrepTime);
            IntVar prepTimeDiff = model.newIntVar(-dayCount, uniClass.idealPrepTime, uniClass.name + "prepDiff");
            LinearExpr expr = LinearExpr.scalProd(new IntVar[] {idealPrepTime, duration, one}, new int[] {1, -1, 1});
            model.addEquality(expr, prepTimeDiff);

            // prepTimeDiffPos = max(0, prepTimeDiff)
            IntVar prepTimeDiffPos = model.newIntVar(0, uniClass.idealPrepTime, uniClass.name + "prepDiffPos");
            model.addMaxEquality(prepTimeDiffPos, new IntVar[] {prepTimeDiff, zero});

            lossesCoefs.add(uniClass.getImportance(w));
            lossesVars.add(prepTimeDiffPos);
        }

        model.addNoOverlap(intervals.toArray(IntervalVar[]::new));
        model.minimize(
                LinearExpr.scalProd(
                        lossesVars.toArray(IntVar[]::new),
                        lossesCoefs.stream().mapToInt(i -> i).toArray()
                )
        );
    }

    private Stream<LocalDate> getDates(List<UniversityClass> classes) {
        return classes.stream().flatMap(c -> c.exams.stream().map(e -> e.date));
    }

    // ADAM Existuje lepší způsob jak převést List<Integer> na int[][]?
    private int[][] toArray(List<Integer> list) {
        int[][] result = new int[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            result[i][0] = list.get(i);
        }
        return result;
    }

    // Return a list of days which are just before an exam
    private int[][] getEndDays(UniversityClass uniClass, LocalDate firstDay) {
        List<Integer> resultList = uniClass.exams.stream()
                .map(e -> e.date)
                .map(d -> ChronoUnit.DAYS.between(firstDay, d))
                .map(n -> Math.toIntExact(n))
                .collect(Collectors.toList());
        return toArray(resultList);
    }
}
