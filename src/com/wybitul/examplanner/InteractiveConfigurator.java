package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveConfigurator {
    private static Config.Builder configBuilder = new Config.Builder();
    private static Scanner sc = new Scanner(System.in);

    private static ClassOptions defaultClassOptions = Config.defaultClassOptions;

    private InteractiveConfigurator() { }

    // TODO Fix defaults not being loaded
    public static Config startConfiguration() {
        Map<ClassInfo, List<LocalDate>> classExamDates = getClassExamDates();
        Set<ClassInfo> classInfo = classExamDates.keySet();

        configBuilder.setFirstDate(getFirstDate());
        configBuilder.setWeightsConfig(getWeightsConfig());

        defaultClassOptions = getDefaultClassOptions();

        Set<ClassOptions.Builder> classBuilders = classInfo.stream()
                .map(c -> new ClassOptions.Builder(c, defaultClassOptions))
                .collect(Collectors.toSet());

        msg("Nyní nastavíme základní vlastnosti jednotlivých předmětů." +
                "Pro detailní popis jednotlivých vlastností viz README." +
                "Pokročilé vlastnosti mohou být nastaveny ručně v konfiguračním souboru.");
        classBuilders.forEach(b -> b.setExamDates(classExamDates.getOrDefault(b.classInfo, new ArrayList<>())));

        optionallyDownloadCredits(classBuilders, classInfo);
        classBuilders.forEach(b -> configBuilder.addClassOptions(getClassOptions(b)));

        return configBuilder.createConfig();
    }

    private static LocalDate getFirstDate() {
        msg("Od kdy se můžete začít učit? Zadejte prosím datum ve formátu \"den. měsíc. rok\".");
        return parseDate(sc.nextLine());
    }

    private static void optionallyDownloadCredits(Set<ClassOptions.Builder> builders, Set<ClassInfo> classInfo) {
        msg("Chcete pro vaše předměty nechat ze SISu stáhnout jejich kreditové ohodnocení? (a)no/(n)e");
        String rsp = sc.nextLine();
        while (!rsp.toLowerCase().equals("a") || !rsp.toLowerCase().equals("n")) {
            msg("Odpovězte prosím pouze \"a\" nebo \"n\".");
            rsp = sc.nextLine();
        }

        // Set credits to be equal to the downloaded ones
        if (rsp.equals("a")) {
            HashMap<ID, Integer> creditMap = CreditDownloader.getCredits(classInfo);
            builders.forEach(b -> {
                if (creditMap.containsKey(b.classInfo.id)) {
                    b.setCredits(creditMap.get(b.classInfo.id));
                }
            });
        }
    }

    private static ClassOptions getDefaultClassOptions() {
        msg("Nyní nastavíme výchozí parametry pro všechny předměty (pro každý předmět zvlášť pak půjdou přepsat)." +
                "Pro detailní popis jednotlivých vlastností viz README." +
                "Pokročilé vlastnosti mohou být nastaveny ručně v konfiguračním souboru.");

        var b = new ClassOptions.Builder(defaultClassOptions);

        String m1 = "Jaký je výchozí počet náhradních termínů, které chcete nechat?";
        return getBasicClassOptions(b, m1, "Jaká má být výchozí váha předmětu?");
    }

    private static void ask(String message, String def, Consumer<String> action) {
        msg(String.format("%s [ENTER pro výchozí: %s]", message, def));
        String input = sc.next();
        if (input.isEmpty()) {
            action.accept(def);
        } else {
            try {
                action.accept(input);
            } catch (Exception e) {
                msg(String.format("Byla zadána nesprávná hodnota. Místo ní bude použita výchozí: %s.", def));
                action.accept(def);
            }
        }
    }

    private static LocalDate parseDate(String str) {
        Pattern datePattern = Pattern.compile("^(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})");
        Matcher dateMatcher = datePattern.matcher(str);

        while (!dateMatcher.find()) {
            msg("Chybný formát. Zadejte prosím datum ve formátu \"den. měsíc. rok\".");
            str = sc.nextLine();
            dateMatcher = datePattern.matcher(str);
        }

        int day = Integer.parseInt(dateMatcher.group(1));
        int month = Integer.parseInt(dateMatcher.group(2));
        int year = Integer.parseInt(dateMatcher.group(3));

        return LocalDate.of(year, month, day);
    }

    private static ClassOptions getClassOptions(ClassOptions.Builder b) {
        msg("Nastavení předmětu " + b.classInfo.name);

        ask("Jaký status (P/PVP/V) má předmět?", defaultClassOptions.status.toString(), s -> {
            switch (s.toLowerCase()) {
                case "p":
                    b.setStatus(Status.P);
                    break;
                case "pvp":
                    b.setStatus(Status.PVP);
                    break;
                case "v":
                    b.setStatus(Status.V);
                    break;
            }
        });

        ask("Kolik kreditů je za tento předmět?",
                String.valueOf(b.credits),
                n -> b.setCredits(Integer.parseInt(n)));

        ask("Kolik dní byste v ideálním případě chtěli mít na přípravu na tento předmět?",
                String.valueOf(defaultClassOptions.idealPrepTime),
                n -> b.setIdealPrepTime(Integer.parseInt(n)));

        String m1 = "Kolik chcete nechat náhradních termínů?";
        return getBasicClassOptions(b, m1, "Jaká má být váha tohoto předmětu?");
    }

    private static ClassOptions getBasicClassOptions(ClassOptions.Builder b, String m1, String m2) {
        ask(m1, String.valueOf(defaultClassOptions.backupTries), n -> b.setBackupTries(Integer.parseInt(n)));
        ask(m2, String.valueOf(defaultClassOptions.weight), n -> b.setWeight(Integer.parseInt(n)));

        String m3 = "Jaký den nejdříve by mohla být zkouška? Napište prosím datum ve formátu \"den. měsíc. rok\".";
        String def1 = defaultClassOptions.lowBound == null ? "bez omezení" : defaultClassOptions.lowBound.toString();
        msg(String.format("%s [ENTER pro výchozí: %s]", m3, def1));
        String inp1 = sc.nextLine();
        if (!inp1.isEmpty()) { b.setLowBound(parseDate(inp1)); }

        String m4 = "Jaký den nejpozději by mohla být zkouška? Napište prosím datum ve formátu \"den. měsíc. rok\".";
        String def2 = defaultClassOptions.highBound == null ? "bez omezení" : defaultClassOptions.highBound.toString();
        msg(String.format("%s [ENTER pro výchozí: %s]", m4, def2));
        String inp2 = sc.nextLine();
        if (!inp2.isEmpty()) { b.setHighBound(parseDate(inp2)); }

        return b.createClassOptions();
    }

    private static WeightsConfig getWeightsConfig() {
        WeightsConfig.Builder b = new WeightsConfig.Builder(Config.defaultWeightsConfig);
        msg("Předmětům je přidělován čas na přípravu úměrně k jejich důležitosti. " +
                "Důležitost předmětu je vypočítána jako: v * váha + s * st(status) + k * kredity, " +
                "kde váha, status (P/PVP/V) a kredity jsou vlastnosti předmětu a v, s, st, k jsou, " +
                "globální parametry.");

        String fmt = "Jakou hodnotu chcete aby měl parametr u %s (%s)?";

        ask(String.format(fmt, "váhy", "v"),
                String.valueOf(Config.defaultWeightsConfig.w),
                n -> b.setWeight(Integer.parseInt(n)));
        ask(String.format(fmt, "statusu", "s"),
                String.valueOf(Config.defaultWeightsConfig.s),
                n -> b.setStatus(Integer.parseInt(n)));
        ask(String.format(fmt, "kreditů", "k"),
                String.valueOf(Config.defaultWeightsConfig.c),
                n -> b.setCredit(Integer.parseInt(n)));

        String fmt2 = "Nyní nastavíme funkci st, která přiřazuje číslo statusu předmětu (P/PVP/V). " +
                "Zadejte prosím hodnotu pro %s předměty.";

        StatusFunction st = new StatusFunction(0, 0, 0);
        ask(String.format(fmt2, "povinné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.P)),
                n -> st.p = Integer.parseInt(n));
        ask(String.format(fmt2, "povinně volitelné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.PVP)),
                n -> st.p = Integer.parseInt(n));
        ask(String.format(fmt2, "volitelné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.V)),
                n -> st.p = Integer.parseInt(n));

        b.setStatusFunction(st);
        return b.createWeightsConfig();
    }

    private static Map<ClassInfo, List<LocalDate>> getClassExamDates() {
        msg("Stáhněte si prosím ze SISu xlsx tabulku s termíny zkoušek (návod můžete najít v README)." +
                "Až ji budete mít staženou, zadejte prosím cestu k ní:");
        String path = sc.nextLine();
        var result = ClassParser.parse(path);

        while (result.isEmpty()) {
            msg("Někde se stala chyba. Překontrolujte prosím cestu a zadejte ji znovu.");
            path = sc.nextLine();
            result = ClassParser.parse(path);
        }

        return result.get();
    }

    private static void msg(String message) {
        System.out.println(message);
    }
}
