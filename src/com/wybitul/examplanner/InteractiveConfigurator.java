package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InteractiveConfigurator {
    private static final Config.Builder configBuilder = new Config.Builder();
    private static final Scanner sc = new Scanner(System.in);
    private static int defaultYear = -1;

    private static Set<ID> creditsDownloaded = new HashSet<>();
    private static Set<ID> statusDownloaded = new HashSet<>();

    private InteractiveConfigurator() { }

    // TODO Set detail
    // ADAM Kdybych chtěl ty zprávy ve více jazycích, musel bych je mít uložené v nějakém objektu
    // a používat je jako msg(o.welcome), msg(o.settings) atd, nebo je nějaký lepší způsob?
    public static Config startConfiguration() {
        section("Úvodní nastavení");

        Map<ClassInfo, Set<LocalDate>> classExamDates = getClassExamDates();

        section("Obecná nastavení");

        defaultYear = getDefaultYear();

        msg("Od kdy se můžete začít učit?");
        configBuilder.setBeginning(getDate());

        configBuilder.setWeightsConfig(Config.defaultWeightsConfig); // getWeightsConfig()
        configBuilder.setGlobalClassOptions(getGlobalClassOptions());

        Set<ClassOptions.Builder> classBuilders = classExamDates.keySet().stream()
                .map(c -> new ClassOptions.Builder(c, configBuilder.globalClassOptions, defaultYear))
                .collect(Collectors.toSet());

        section("Nastavení jednotlivých předmětů");

        classBuilders.forEach(b -> b.setExamDates(classExamDates.getOrDefault(b.classInfo, new HashSet<>())));
        optionallyParseCreditsAndStatus(classBuilders);
        classBuilders.forEach(b -> configBuilder.addClassOptions(getClassOptions(b)));

        return configBuilder.createConfig();
    }

    private static Map<ClassInfo, Set<LocalDate>> getClassExamDates() {
        header("Nastavení termínů zkoušek");

        msg("Stáhněte si prosím ze SISu xlsx tabulku s termíny zkoušek (návod můžete najít v README).");
        return ask("zadejte prosím cestu k platnému xlsx souboru", ClassParser::parse);
    }

    private static int getDefaultYear() {
        msg("V průběhu konfigurace budete často zadávat různá data.",
                "Abyste si ušetřili čas s jejich psaním, zadejte prosím rok",
                "který bude automaticky přidán ke každému datu bez specifikovaného roku.");

        return ask(
                "zadejte celé číslo mezi 2000 a 3000",
                Integer::parseInt,
                i -> 2000 <= i && i <= 3000
        );
    }

    private static WeightsConfig getWeightsConfig() {
        header("Nastavení výpočtu důležitosti předmětu");

        WeightsConfig.Builder b = new WeightsConfig.Builder();
        msg("Předmětům je přidělován čas na přípravu úměrně k jejich důležitosti.",
                "Důležitost předmětu je vypočítána jako: v * váha + s * st(status) + k * kredity,",
                "kde váha, status (P/PVP/V) a kredity jsou vlastnosti předmětu a v, s, st, k jsou,",
                "globální parametry.");

        newline();

        String fmt = "Jakou hodnotu chcete aby měl parametr u %s (%s)?";

        msg(String.format(fmt, "váhy", "v"));
        b.setWeight(getPositiveNumber(Config.defaultWeightsConfig.w));

        msg(String.format(fmt, "statusu", "s"));
        b.setStatus(getPositiveNumber(Config.defaultWeightsConfig.s));

        msg(String.format(fmt, "kreditů", "k"));
        b.setCredit(getPositiveNumber(Config.defaultWeightsConfig.c));

        header("Nastavení statusové funkce (st)");

        String fmt2 = "Zadejte prosím hodnotu pro %s předměty.";

        msg(String.format(fmt2, "povinné"));
        int p = getNumber(Config.defaultWeightsConfig.st.apply(Status.P));

        msg(String.format(fmt2, "povinně volitelné"));
        int pvp = getNumber(Config.defaultWeightsConfig.st.apply(Status.PVP));

        msg(String.format(fmt2, "volitelné"));
        int v = getNumber(Config.defaultWeightsConfig.st.apply(Status.V));

        return b.setStatusFunction(new StatusFunction(p, pvp, v)).createWeightsConfig();
    }

    private static void optionallyParseCreditsAndStatus(Set<ClassOptions.Builder> builders) {
        header("Automatické předvyplnění údajů");

        msg("Doporučujeme vám stáhnout stránku ze SISu nazvanou \"Zápis předmětů a rozvrhu\",",
                "s její pomocí totiž budeme schopní vyplnit spoustu informací o předmětech automaticky",
                "(jinak byste je museli vypisovat ručně).");

        Optional<CreditsAndStatusParser> parserOpt = ask(
                "zadejte cestu k platnému html souboru",
                p -> CreditsAndStatusParser.makeParser(p),
                Optional::isPresent,
                Optional.empty(),
                "přeskočit tento krok"
        );

        if (parserOpt.isEmpty()) {
            optionallyDownloadCredits(builders);
            return;
        }

        var parser = parserOpt.get();
        var credits = parser.getCreditsMap();
        var statuses = parser.getStatusesMap();
        creditsDownloaded.addAll(credits.keySet());
        statusDownloaded.addAll(statuses.keySet());
        builders.forEach(b -> {
            ID id = b.classInfo.id;
            if (credits.containsKey(id)) { b.setCredits(credits.get(id)); }
            if (statuses.containsKey(id)) { b.setStatus(statuses.get(id)); }
        });
    }

    private static Optional<Boolean> parseBoolean(String s) {
        switch (s.toLowerCase()) {
            case "a":
                return Optional.of(Boolean.TRUE);
            case "n":
                return Optional.of(Boolean.FALSE);
            default:
                return Optional.empty();
        }
    }

    private static void optionallyDownloadCredits(Set<ClassOptions.Builder> builders) {
        msg("Chcete alespoň nechat ze SISu stáhnout kreditové ohodnocení jednotlivých předmětů?");

        Boolean shouldDownload = ask(
                "Odpovězte prosím \"a\" nebo \"n\".",
                InteractiveConfigurator::parseBoolean
        );

        // Set credits to be equal to the downloaded ones
        if (shouldDownload) {
            List<ID> ids = builders.stream()
                    .map(b -> b.classInfo.id)
                    .collect(Collectors.toList());

            msg("Chvilku strpení, stahuji informaci o kreditech...");
            HashMap<ID, Integer> creditMap = CreditDownloader.getCredits(ids);
            builders.forEach(b -> {
                ID id = b.classInfo.id;
                if (creditMap.containsKey(id)) { b.setCredits(creditMap.get(id)); }
            });
            msg("Hotovo");
        }
        newline();
    }

    private static ClassOptions getGlobalClassOptions() {
        header("Nastavení globálních parametrů");

        msg("Nyní nastavíme globální parametry (pro každý předmět zvlášť pak půjdou přepsat).",
                "Pro detailní popis jednotlivých parametrů viz README.",
                "Pokročilé parametry mohou být nastaveny ručně v konfiguračním souboru.");

        var b = new ClassOptions.Builder(configBuilder.globalClassOptions, defaultYear);

        newline();

        msg("Kolik chcete nechat náhradních termínů?");
        b.setBackupTries(getPositiveNumber(configBuilder.globalClassOptions.backupTries));

        msg("Jaká chcete aby byla výchozí váha předmětu?");
        b.setWeight(getNumber(configBuilder.globalClassOptions.weight));

        msg("Kdy nejdříve můžete dělat zkoušku?");
        Optional<LocalDate> lowBound = ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                Optional::isPresent,
                Optional.empty(),
                "bez omezení"
        );

        msg("Kdy nejpozději můžete dělat zkoušku?");
        Optional<LocalDate> highBound = ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                Optional::isPresent,
                Optional.empty(),
                "bez omezení"
        );

        return b.setLowBound(lowBound).setHighBound(highBound).createClassOptions();
    }

    private static ClassOptions getClassOptions(ClassOptions.Builder b) {
        header("Nastavení předmětu", b.classInfo.name);

        msg("Kolik dní byste v ideálním případě chtěli mít na přípravu na tento předmět?");
        b.setIdealPrepTime(getPositiveNumber(configBuilder.globalClassOptions.idealPrepTime));

        msg("Kolik chcete nechat náhradních termínů?");
        b.setBackupTries(getPositiveNumber(configBuilder.globalClassOptions.backupTries));

        if (!creditsDownloaded.contains(b.classInfo.id)) {
            msg("Kolik kreditů je za tento předmět?");
            b.setCredits(getPositiveNumber(b.credits));
        }

        if (!statusDownloaded.contains(b.classInfo.id)) {
            msg("Je tento předmět povinný, povinně volitelný nebo volitelný?");
            Status status = ask(
                    "Zadejte P, PVP nebo V.",
                    s -> Status.valueOf(s.toUpperCase()),
                    i -> true,
                    configBuilder.globalClassOptions.status
            );
            b.setStatus(status);
        }

        msg("Jakou váhu chcete přiřadit tomuto předmětu?");
        b.setWeight(getNumber(configBuilder.globalClassOptions.weight));

        return b.createClassOptions();
    }

    private static LocalDate getDate() {
        return ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear)
        );
    }

    private static LocalDate getDate(LocalDate def, String defDescription) {
        return ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                def,
                defDescription
        );
    }

    private static int getNumber(int def) {
        return ask(
                "zadejte prosím celé číslo",
                Integer::parseInt,
                i -> true,
                def
        );
    }

    private static int getPositiveNumber(int def) {
        return ask(
                "zadejte prosím celé nezáporné číslo",
                Integer::parseInt,
                i -> i >= 0,
                def
        );
    }

    // `ask` with the predicate automatically set to Optional::isPresent, because `trans` returns optional,
    // and a default value if the user enters ""
    private static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred, T def) {
        return ask(spec, trans, pred, def, String.valueOf(def));
    }

    // `ask` with the predicate automatically set to Optional::isPresent, because `trans` returns optional,
    // and a default value if the user enters ""
    private static <T> T ask(String spec, Function<String, Optional<T>> trans, T def, String defStr) {
        return ask(spec, trans, Optional::isPresent, Optional.of(def), defStr).get();
    }

    // `ask` with the predicate automatically set to Optional::isPresent, because `trans` returns optional
    private static <T> T ask(String spec, Function<String, Optional<T>> trans) {
        return ask(spec, trans, Optional::isPresent).get();
    }

    // `ask` with a default value if user enters ""
    // ADAM Jak správně formátovat víceřádkovou deklaraci?
    private static <T> T ask(String spec, Function<String, T> trans,
                             Predicate<T> pred, T def, String defString) {
        Optional<T> result = ask(
                String.format("%s, nebo pro zachování výchozí hodnoty (%s) nechte vstup prázdný", spec, defString),
                s -> s.isEmpty() ? Optional.empty() : Optional.of(trans.apply(s)),
                t -> t.isEmpty() || pred.test(t.get()),
                false
        );

        if (result.isEmpty()) { msg(String.format("(nastaveno: %s)", defString)); }
        newline();

        return result.orElse(def);
    }

    // `ask` with newline after it
    private static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred) {
        return ask(spec, trans, pred, true);
    }

    // Ask the user to enter information according to `spec`, transform in to T with `trans`,
    // and check it with the predicate `pred`
    // If `trans` fails or `pred` returns false, ask the user to enter the information again
    private static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred, boolean newline) {
        T tInput = null;
        String input;
        boolean test = false;

        msg(String.format("[%s]", spec));
        while (tInput == null || !test) {
            input = sc.nextLine();
            try {
                tInput = trans.apply(input);
                test = pred.test(tInput);
                if (!test) { msg(String.format("Nesprávný vstup, %s.", spec)); }
            } catch (Exception e) {
                msg(String.format("Nesprávný vstup, %s.", spec));
            }
        }

        if (newline) { newline(); }
        return tInput;
    }

    private static void section(String ...messages) {
        String text = String.join(" ", messages);
        msg(text);
        msg("=".repeat(text.length()));
        newline();
    }

    private static void header(String ...messages) {
        String text = String.join(" ", messages);
        msg(text);
        msg("-".repeat(text.length()));
    }

    private static void msg(String ...messages) {
        System.out.println(String.join(" ", messages));
    }

    private static void newline() { msg(); }
}
