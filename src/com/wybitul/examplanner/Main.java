package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) throws Exception {
        var w = new WeightConfigurator(t -> 5, 1, 1, 1);

        Map<String, Info> info = InfoParser.parse(args[0]);
        Map<String, Config> config = ConfigParser.parse(args[1]);
        List<UniversityClass> classes = new ArrayList<>();
        config.forEach((key, c) -> {
            if (info.containsKey(key)) {
                classes.add(new UniversityClass(key, info.get(key), c));
            }
        });

        LocalDate firstExam = classes.stream()
                .flatMap(c -> c.exams.stream().map(e -> e.date))
                .min(Comparator.naturalOrder())
                .get();

        LocalDate firstDate = classes.stream()
                .map(c -> c.lowBound)
                .min(Comparator.naturalOrder())
                .orElse(firstExam);

        Model model = new Model(classes, firstDate, w);
        Solver solver = new Solver(model, firstDate);
        List<Result> results = solver.solve();
        results.stream()
                .sorted(Comparator.comparing(r -> r.exam.date))
                .forEach(r -> System.out.printf("%s: %s\n", r.uniClass.name, r.exam.date));
    }
}
