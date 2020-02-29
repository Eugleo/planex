package com.wybitul.examplanner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    static {
        System.loadLibrary("jniortools");
    }

    // TODO Refactor dialogs
    public static void main(String[] args) {
        Config config;
        if (args.length == 0) {
            System.out.println("Pokud již máte konfigurační soubor, zadejte cestu k němu. " +
                    "Pokud ne, nezadávejte nic; spustí se interaktivní konfigurátor, který vás provede " +
                    "vytvořením nového konfiguračního souboru.");
            String line = sc.nextLine();

            if (line.isEmpty()) {
                config = createNewConfig();
            } else {
                config = loadConfigFromFile(line);
            }
        } else {
            config = loadConfigFromFile(args[0]);
        }

        try {
            Model model = new Model(config);
            Solver solver = new Solver(model);
            Set<Result> results = solver.solve(s -> System.out.printf("Řešení: %s\n\n", s.toString()));
            printResults(results);
        } catch (ModelException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Config loadConfigFromFile(String path) {
        Optional<Config> optConfig = ConfigParser.parse(path);

        while (optConfig.isEmpty()) {
            System.out.println("Při čtení souboru se vyskytla chyba. Zadejte prosím cestu znovu, nebo " +
                    "ponechte vstup prázdný pro spuštění interaktivního konfigurátoru.");
            String line = sc.nextLine();
            if (line.isEmpty()) {
                return createNewConfig();
            }
            optConfig = ConfigParser.parse(line);
        }

        return optConfig.get();
    }

    private static Config createNewConfig() {
        Config cfg = InteractiveConfigurator.startConfiguration();
        System.out.println("Zadejte cestu, kam si přejete tento konfigurační soubor uložit.");

        boolean writeSuccessful = false;
        while (!writeSuccessful) {
            String path = sc.nextLine();
            try {
                writeSuccessful = ConfigWriter.write(cfg, path);
            } catch (Exception e) {
                System.out.println("Ukládání souboru se nepovedlo. Zadejte prosím cestu znovu.");
            }
        }

        return cfg;
    }

    private static void printResults(Collection<Result> results) {
        List<Result> sorted = results.stream()
                .sorted(Comparator.comparing(r -> r.examDate))
                .collect(Collectors.toList());
        WordFormatter days = new WordFormatter("dní", "den", "dny");
        WordFormatter tries = new WordFormatter("pokusů", "pokus", "pokusy");

        Matrix<Object> resultTable = new Matrix<>();
        Stream.of(
                sorted.stream().map(r -> r.classOptions.classInfo.name),
                sorted.stream()
                        .map(r -> r.start)
                        .map(d -> Utils.formatDate(d, -1)),
                sorted.stream()
                        .map(r -> r.examDate)
                        .map(d -> Utils.formatDate(d, -1)),
                sorted.stream()
                        .map(r -> {
                            int dif = r.prepTime - r.classOptions.idealPrepTime;
                            String difStr = dif > 0 ? "+" + dif : String.valueOf(dif);
                            return String.format("%s (%s)", days.format(r.prepTime), difStr);
                        }),
                sorted.stream().map(r -> tries.format(r.backupTries))
        ).forEach(c -> resultTable.addColumn(c.collect(Collectors.toList())));

        List<Object> header = List.of(
                "předmět",
                "začátek přípravy",
                "termín zkoušky",
                "čas na přípravu",
                "zbývá pokusů"
        );

        resultTable.addRow(0, header);
        printTable(resultTable);
    }

    private static void printTable(Matrix<Object> table) {
        List<Integer> lengths = table.getColumns().stream()
                .map(c -> c.stream().map(o -> o.toString().length()).max(Comparator.naturalOrder()).orElse(0))
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
            System.out.println();
        });
    }
}
