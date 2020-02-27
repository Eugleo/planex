package com.wybitul.examplanner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    private static Config config;

    static {
        System.loadLibrary("jniortools");
    }

    // TODO Add text walkthrough (open existing (or if it doesn't work:) create new and save it)
    public static void main(String[] args) {
        if (args.length == 0) {
            // TODO Add config saving
            config = InteractiveConfigurator.startConfiguration();
        } else {
            Optional<Config> optConfig = ConfigParser.parse(args[0]);
            optConfig.ifPresentOrElse(
                    c -> config = c,
                    () -> System.out.println("Unable to parse the configuration file.")
            );
        }

        try {
            Model model = new Model(config);
            Solver solver = new Solver(model);
            Set<Result> results = solver.solve(s -> System.out.println("Solution: " + s.toString()));
            printResults(results);
        } catch (ModelException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printResults(Collection<Result> results) {
        Stream<Result> sorted = results.stream().sorted(Comparator.comparing(r -> r.examDate));
        WordFormatter days = new WordFormatter("dní", "den", "dny");
        WordFormatter tries = new WordFormatter("pokusů", "pokus", "pokusy");

        Matrix<Object> resultTable = new Matrix<>();
        Stream.of(
                sorted.map(r -> r.classOptions.classInfo.name),
                sorted.map(r -> r.start),
                sorted.map(r -> r.examDate),
                sorted.map(r -> String.format("%d/%s", r.prepTime, days.format(r.classOptions.idealPrepTime))),
                sorted.map(r -> tries.format(r.backupTries))
        ).forEach(c -> resultTable.addColumn(c.collect(Collectors.toList())));
        List<Object> header = List.of("předmět", "začátek přípravy", "termín zkoušky", "délka přípravy", "zbývá pokusů");
        resultTable.addRow(0, header);

        printTable(resultTable);
    }

    private static void printTable(Matrix<Object> table) {
        List<Integer> lengths = table.getColumns().stream()
                .map(c -> c.stream().map(o -> o.toString().length()).max(Comparator.naturalOrder()).get())
                .collect(Collectors.toList());

        Object[][] tableArray = table.toArray(Object.class);

        IntStream.range(0, table.getRows().size()).forEach(i -> {
            String formatBit;
            if (i == 0) {
                formatBit = "S ";
            } else {
                formatBit = "s ";
            }
            IntStream.range(0, table.getColumns().size()).forEach(j -> {
                String align;
                if (j == 0) {
                    align = "%-";
                } else {
                    align = "%";
                }
                System.out.printf(align + lengths.get(j) + formatBit, tableArray[i][j]);
            });
        });
    }
}
