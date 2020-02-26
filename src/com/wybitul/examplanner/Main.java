package com.wybitul.examplanner;

import java.util.*;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) throws Exception {
        try {
            Config config = ConfigParser.parse(args[0]);
            Map<String, ExamInfo> info = ExamInfoParser.parse(config.excelFilePath);
            List<UniversityClass> classes = new ArrayList<>();
            info.forEach((key, in) -> {
                ClassParams cp = config.classParams.getOrDefault(key, config.classParams.get("default"));
                classes.add(new UniversityClass(key, in, cp));
            });

            Model model = new Model(classes, config.firstDate, config.weightConfigurator);
            Solver solver = new Solver(model, config.firstDate);
            List<Result> results = solver.solve();
            results.stream()
                    .sorted(Comparator.comparing(r -> r.exam.date))
                    .forEach(r -> System.out.printf("%s: %s\n", r.uniClass.name, r.exam.date));
        } catch (MissingFieldException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
