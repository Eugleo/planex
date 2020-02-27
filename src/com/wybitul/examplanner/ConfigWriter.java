package com.wybitul.examplanner;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigWriter {
    private static PrintStream out;
    private static ClassOptions defOpts;

    private ConfigWriter() { }

    public static void write(Config config, String path) {
        try (PrintStream ps = new PrintStream(new FileOutputStream(new File(path)))) {
            out = ps;
            defOpts = config.classOptions.stream().filter(opt -> opt.classInfo == null).findFirst().get();

            comment("Tento konfigurační soubor byl vytvořený " + LocalDate.now());
            newline();
            comment("Datum začátku učení");
            writeOption("začátek", config.beginning);
            newline();
            writeWeightsConfig(config.weightsConfig);
            out.println("+++");
            newline();
            config.classOptions.forEach(ConfigWriter::writeClassOptions);
        } catch (FileNotFoundException e) {
            System.out.println("Can't open file " + path);
        }
    }

    private static void writeWeightsConfig(WeightsConfig cfg) {
        comment("Nastavení parametrů důležitosti");
        comment("důležitost předmětu = v * váha + k * kredity + s * st(status)");
        writeOption("v", cfg.w);
        writeOption("k", cfg.c);
        writeOption("s", cfg.s);
        newline();
        comment("St je funkce přiřazující číslo každému ze statusů (P/PVP/V)");
        comment("zadání: st(P), st(PVP), st(V)");
        Stream<String> str = Arrays.stream(new int[] {cfg.st.p, cfg.st.pvp, cfg.st.v}).mapToObj(String::valueOf);
        writeOption("st", String.join(", ", str.collect(Collectors.toList())));
        newline();
    }

    private static void writeClassOptions(ClassOptions opts) {
        WordFormatter days = new WordFormatter("dní", "den", "dny");

        List<String> exams = opts.examDates.stream()
                .sorted(Comparator.naturalOrder())
                .map(ConfigWriter::formatDate)
                .collect(Collectors.toList());

        List<String> defaultExams = defOpts.examDates.stream()
                .sorted(Comparator.naturalOrder())
                .map(ConfigWriter::formatDate)
                .collect(Collectors.toList());

        Map<String, Object> options = Map.of(
                "kredity", opts.credits,
                "status", opts.status.name(),
                "váha", opts.weight,
                "termíny", opts.backupTries,
                "příprava", days.format(opts.idealPrepTime),
                "minimum", days.format(opts.minPrepTime),
                "datum", String.format("%s - %s", formatDate(opts.lowBound), formatDate(opts.highBound)),
                "zkoušky", String.join(", ", exams)
        );

        Map<String, Object> defaultOptions = Map.of(
                "kredity", defOpts.credits,
                "status", defOpts.status.name(),
                "váha", defOpts.weight,
                "termíny", defOpts.backupTries,
                "příprava", days.format(defOpts.idealPrepTime),
                "minimum", days.format(defOpts.minPrepTime),
                "datum", String.format("%s - %s", formatDate(defOpts.lowBound), formatDate(defOpts.highBound)),
                "zkoušky", String.join(", ", defaultExams)
        );

        if (opts == defOpts) {
            comment("Následují globální nastavení, která platí plošně");
            comment("pro každý předmět, u kterého nejsou přepsána");
            comment("Pro jejich vysvětlení viz README.");

            options.forEach(ConfigWriter::writeOption);
        } else {
            out.printf("= %s (%s)\n", opts.classInfo.name, opts.classInfo.id.str);
            options.forEach((opt, val) -> {
                if (!defaultOptions.get(opt).equals(val)) {
                    writeOption(opt, val);
                }
            });
        }

        if (opts.classInfo != null && opts.classInfo.type == Type.COLLOQUIUM) { writeFlag("zápočet"); }
        if (opts.ignore) { writeFlag("ignorovat"); }
        newline();
    }

    private static String formatDate(LocalDate date) {
        return date == null ?
                "x" :
                String.format("%d. %d. %d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private static void writeOption(String name, Object value) {
        out.printf("- %s: %s\n", name, value.toString());
    }

    private static void writeFlag(String name) {
        out.printf("- %s\n", name);
    }

    private static void comment(String str) {
        out.println("; " + str);
    }

    private static void newline() {
        out.println();
    }
}
