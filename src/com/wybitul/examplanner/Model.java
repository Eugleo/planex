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

    LocalDate firstDate;

    // TODO Change exception to a better one
    Model(List<UniversityClass> classes, LocalDate firstDate, WeightConfigurator w) throws Exception {
        this.classes = classes;
        this.firstDate = firstDate;

        LocalDate lastDate = getDates(classes).max(Comparator.naturalOrder()).orElseThrow(() -> new Exception());
        int dayCount = Math.toIntExact(ChronoUnit.DAYS.between(firstDate, lastDate));
        List<IntervalVar> intervals = new ArrayList<>();
        List<IntVar> durations = new ArrayList<>();
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

            starts.add(start);
            ends.add(end);
            durations.add(duration);
            intervals.add(interval);

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

    // TODO Isn't there a better way to get int[][] from List<Integer>?
    private int[][] toArray(List<Integer> list) {
        // TODO Are the dimensions right
        int[][] result = new int[1][list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[0][i] = list.get(i);
        }
        return result;
    }

    // Return a list of days which are after some exam
    private int[][] getStartDays(List<UniversityClass> classes, LocalDate firstDay) {
        List<Integer> resultList = getDates(classes)
                .map(d -> d.plusDays(1))
                .map(d -> ChronoUnit.DAYS.between(firstDay, d))
                .map(n -> Math.toIntExact(n))
                .collect(Collectors.toList());
        return toArray(resultList);
    }

    // Return a list of days which are just before an exam
    private int[][] getEndDays(UniversityClass uniClass, LocalDate firstDay) {
        List<Integer> resultList = uniClass.exams.stream()
                .map(e -> e.date)
                .map(d -> d.minusDays(1))
                .map(d -> ChronoUnit.DAYS.between(firstDay, d))
                .map(n -> Math.toIntExact(n))
                .collect(Collectors.toList());
        return toArray(resultList);
    }
}
