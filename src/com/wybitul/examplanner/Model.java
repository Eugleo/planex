package com.wybitul.examplanner;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Model {
    CpModel model = new CpModel();
    List<UniversityClass> classes;

    // TODO Implement
    LocalDate firstDate = LocalDate.now();

    Model(List<UniversityClass> classes, WeightConfigurator w) {
        classes = classes;

        // TODO implement max day count (firstDate - lastExamDate)
        int dayCount = 64;
        List<IntVar> durations = new ArrayList<>();
        List<IntervalVar> intervals = new ArrayList<>();
        List<Integer> lossesCoefs = new ArrayList<>();
        List<IntVar> lossesVars = new ArrayList<>();

        IntVar zero = model.newConstant(0);

        for (UniversityClass uniClass: classes) {
            List<UniversityClass> otherClasses = classes.stream()
                    .filter(c -> !c.equals(uniClass))
                    .collect(Collectors.toList());
            int[][] startDays = getStartDays(otherClasses, firstDate);
            int[][] endDays = getEndDays(uniClass, firstDate);

            IntVar start = model.newIntVar(0, dayCount, uniClass.name + "start");
            IntVar duration = model.newIntVar(0, dayCount, uniClass.name + "duration");
            IntVar end = model.newIntVar(0, dayCount, uniClass.name + "end");
            IntervalVar interval = model.newIntervalVar(start, duration, end, uniClass.name + "interval");

            try {
                model.addAllowedAssignments(new IntVar[] {start}, startDays);
                model.addAllowedAssignments(new IntVar[] {end}, endDays);
            } catch (CpModel.WrongLength wrongLength) {
                System.out.println("Incorrect tuple length");
            }

            // prepTimeDiff = idealPrepTime - duration
            IntVar idealPrepTime = model.newConstant(uniClass.idealPrepTime);
            IntVar prepTimeDiff = model.newIntVar(-dayCount, uniClass.idealPrepTime, uniClass.name + "prepDiff");
            LinearExpr expr = LinearExpr.scalProd(new IntVar[] {idealPrepTime, duration}, new int[] {1, -1});
            model.addEquality(expr, prepTimeDiff);

            // prepTimeDiffPos = max(0, prepTimeDiff)
            IntVar prepTimeDiffPos = model.newIntVar(0, uniClass.idealPrepTime, uniClass.name + "prepDiffPos");
            model.addMaxEquality(prepTimeDiffPos, new IntVar[] {prepTimeDiff, zero});

            durations.add(duration);
            intervals.add(interval);
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

    // TODO Implement
    private int[][] getStartDays(List<UniversityClass> classes, LocalDate firstDay) {
        classes.stream()
                .flatMap(c -> c.exams.stream().map(e -> e.date))
                .map(d -> d.plusDays(1))
                .collect(Collectors.toList());

    }

    private int[][] getEndDays(UniversityClass uniClass, LocalDate firstDay) {
        // TODO Are the dimensions right
        int[][] result = new int[1][uniClass.exams.size()];
        List<Integer> resultList = uniClass.exams.stream()
                .map(e -> e.date)
                .map(d -> ChronoUnit.DAYS.between(d, firstDay))
                .map(n -> Math.toIntExact(n))
                .collect(Collectors.toList());

        for (int i = 0; i < resultList.size(); i++) {
            result[0][i] = resultList.get(i);
        }
        return result;
    }
}
