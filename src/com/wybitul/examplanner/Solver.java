package com.wybitul.examplanner;

import com.google.ortools.sat.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Solver {
    Model model;
    CpSolver solver = new CpSolver();
    LocalDate beginning;

    Solver(Model model) {
        this.model = model;
        this.beginning = model.beginning;
    }

    public Set<Result> solve(Consumer<CpSolverStatus> statusConsumer) {
        CpSolverStatus status = solver.solve(model.model);
        if (statusConsumer != null) { statusConsumer.accept(status); }

        return model.classModels.stream()
                .map(cm -> new Result(cm.classOptions, varToDate(cm.end), varToDate(cm.start),
                        varToInt(cm.backupTries), varToInt(cm.preparationTime)))
                .collect(Collectors.toSet());
    }

    private int varToInt(IntVar n) {
        return Math.toIntExact(solver.value(n));
    }

    private LocalDate varToDate(IntVar n) {
        return beginning.plusDays(solver.value(n));
    }
}
