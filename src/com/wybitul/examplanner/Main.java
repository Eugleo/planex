package com.wybitul.examplanner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) {
        Config config;
        if (args.length > 0) {
            config = ConfigParser.parse(args[0])
                    .map(Main::maybeEditConfig)
                    .orElseGet(() -> {
                        Asker.msg("Soubor se nepodařilo načíst. Zkuste prosím zadat cestu znovu.");
                        return getConfig();
                    });
        } else {
            Asker.msg("Pokud již máte konfigurační soubor, zadejte cestu k němu.");
            config = getConfig();
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

    private static Config maybeEditConfig(Config c) {
        Asker.msg("Chtěli byste tento konfigurační soubor dále upravit",
                "v interaktivním konfigurátoru?");
        Boolean shouldEdit = Asker.ask(
                "odpovězte prosím \"a\" nebo \"n\"",
                Utils::parseBoolean,
                false,
                "neupravovat"
        );
        return shouldEdit ? editConfigInConfigurator(c) : c;
    }

    private static Config getConfig() {
        return Asker.ask(
                "zadejte cestu ke konfiguračnímu souboru",
                ConfigParser::parse,
                "vytvořit nový konfigurační soubor"
        )
                .map(Main::maybeEditConfig)
                .orElseGet(Main::newConfig);
    }

    private static Config newConfig() {
        return editConfigInConfigurator(new Config.Builder().createConfig());
    }

    private static Config editConfigInConfigurator(Config config) {
        Config cfg = new InteractiveConfigurator(new Config.Builder(config)).startConfiguration();

        Asker.section("Uložení souboru");
        Asker.msg("Zadejte cestu, kam si přejete tento konfigurační soubor uložit.");
        Asker.ask(
                "zadejte platnou cestu bez koncovky",
                Function.identity(),
                (String p) -> !p.matches("^.*\\.[^/\\\\.]+$") && ConfigWriter.write(cfg, p + ".plx")
        );

        return cfg;
    }

    private static void printResults(Collection<Result> results) {
        if (results.size() == 0) { return; }

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
