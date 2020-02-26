package com.wybitul.examplanner;

import com.google.ortools.sat.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Solver {
    Model model;
    CpSolver solver = new CpSolver();
    LocalDate firstDate;

    Solver(Model model, LocalDate firstDate) {
        this.model = model;
        this.firstDate = firstDate;
    }

    public List<Result> solve() {
        CpSolverStatus status = solver.solve(model.model);
        //System.out.println(status);
        List<LocalDate> examDates = model.ends.stream()
                .map(v -> solver.value(v))
                .map(i -> firstDate.plusDays(i))
                .collect(Collectors.toList());

        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < examDates.size(); i++) {
            int finalI = i;
            Exam exam = model.classes.get(i).exams.stream()
                    .filter(e -> e.date.equals(examDates.get(finalI)))
                    .findFirst()
                    .orElseThrow();
            exams.add(exam);
        }

        List<Result> result = new ArrayList<>();
        for (int i = 0; i < exams.size(); i++) {
            Result r = new Result(model.classes.get(i), exams.get(i), solver.value(model.durations.get(i)) - 1);
            result.add(r);
        }

        return result;
    }
}
