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
                if (!cp.ignore) {
                    classes.add(new UniversityClass(key, in, cp));
                }
            });

            Model model = new Model(classes, config.firstDate, config.weightConfigurator);
            Solver solver = new Solver(model, config.firstDate);
            List<Result> results = solver.solve();
            printResults(results);
        } catch (MissingFieldException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printResults(List<Result> results) {
        int fst = results.stream().map(r -> r.uniClass.name.length()).max(Comparator.naturalOrder()).orElseGet("předmět"::length);
        int snd = "2020-01-10".length();
        int thd = "příprava".length();
        String formatString1 = "%-" + fst + "S %" + snd + "S %" + thd + "S\n";
        System.out.printf(formatString1, "předmět", "zkouška", "příprava");
        String formatString2 = "%-" + fst + "s %" + snd + "s %" + thd + "s\n";
        results.stream()
                .sorted(Comparator.comparing(r -> r.exam.date))
                .forEach(r -> {
                    String fmt = String.format("%d (%d)", r.prepTime, r.uniClass.idealPrepTime);
                    System.out.printf(formatString2, r.uniClass.name, r.exam.date, fmt);
                });
    }
}
