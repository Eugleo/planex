package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InteractiveConfigurator {
    private static final Config.Builder configBuilder = new Config.Builder();
    private static final Scanner sc = new Scanner(System.in);
    private static int defaultYear = -1;

    private InteractiveConfigurator() { }

    public static Config startConfiguration() {
        Map<ClassInfo, Set<LocalDate>> classExamDates = getClassExamDates();
        Set<ClassInfo> classInfo = classExamDates.keySet();

        header("Obecná nastavení");

        defaultYear = getDefaultYear();

        msg("Od kdy se můžete začít učit?");
        configBuilder.setBeginning(getDate());
        configBuilder.setWeightsConfig(getWeightsConfig());
        configBuilder.setUserDefaultOpts(getDefaultClassOptions());

        Set<ClassOptions.Builder> classBuilders = classInfo.stream()
                .map(c -> new ClassOptions.Builder(c, configBuilder.userDefaultOpts, defaultYear))
                .collect(Collectors.toSet());

        classBuilders.forEach(b -> b.setExamDates(classExamDates.getOrDefault(b.classInfo, new HashSet<>())));

        optionallyDownloadCredits(classBuilders, classInfo);
        classBuilders.forEach(b -> configBuilder.addClassOptions(getClassOptions(b)));

        return configBuilder.createConfig();
    }

    private static int getDefaultYear() {
        msg("V průběhu konfigurace budete často zadávat různá data.",
                "Abyste si ušetřili čas s jejich psaním, zadejte prosím rok (ve formátu YYYY),",
                "který bude automaticky přidán ke každému datu bez specifikovaného roku.");

        Integer result = Utils.safeParseInt(sc.nextLine());
        while (result == null || result < 2000 || result > 2999) {
            msg("Prosím vložte čtyřmístné číslo mezi 2000 a 2999.");
            result = Utils.safeParseInt(sc.nextLine());
        }

        newline();
        return result;
    }

    private static void optionallyDownloadCredits(Set<ClassOptions.Builder> builders, Set<ClassInfo> classInfo) {
        msg("Chcete nechat ze SISu stáhnout kreditové ohodnocení vašich předmětů? (a)no/(n)e");
        String rsp = sc.nextLine();
        while (!rsp.toLowerCase().equals("a") && !rsp.toLowerCase().equals("n")) {
            msg("Odpovězte prosím pouze \"a\" nebo \"n\".");
            rsp = sc.nextLine();
        }

        // Set credits to be equal to the downloaded ones
        if (rsp.equals("a")) {
            msg("Chvilku strpení, stahuji informaci o kreditech...");
            List<ID> ids = classInfo.stream().map(info -> info.id).collect(Collectors.toList());
            HashMap<ID, Integer> creditMap = CreditDownloader.getCredits(ids);
            builders.forEach(b -> {
                if (creditMap.containsKey(b.classInfo.id)) {
                    b.setCredits(creditMap.get(b.classInfo.id));
                }
            });
            msg("Hotovo");
        }

        newline();
    }

    private static ClassOptions getDefaultClassOptions() {
        header("Nastavení výchozích parametrů");
        msg("Nyní nastavíme výchozí parametry pro všechny předměty",
                "(pro každý předmět zvlášť pak půjdou přepsat).",
                "Pro detailní popis jednotlivých vlastností viz README.",
                "Pokročilé vlastnosti mohou být nastaveny ručně v konfiguračním souboru.");

        var b = new ClassOptions.Builder(configBuilder.userDefaultOpts, defaultYear);

        String m1 = "Jaký je výchozí počet náhradních termínů, které chcete nechat?";
        return getBasicClassOptions(b, m1, "Jaká má být výchozí váha předmětu?");
    }

    private static void ask(String message, String def, Consumer<String> action) {
        msg(String.format("%s [ENTER pro výchozí: %s]", message, def));
        String input = sc.nextLine();
        if (input.isEmpty()) {
            msg(String.format("(nastaveno: %s)", def));
            action.accept(def);
        } else {
            try {
                action.accept(input);
            } catch (Exception e) {
                msg(String.format("Byla zadána nesprávná hodnota. Místo ní bude použita výchozí: %s.", def));
                action.accept(def);
            }
        }
        newline();
    }

    private static ClassOptions getClassOptions(ClassOptions.Builder b) {
        header("Nastavení předmětu", b.classInfo.name);

        ask("Jaký status (P/PVP/V) má předmět?", configBuilder.userDefaultOpts.status.toString(), s -> {
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
                String.valueOf(configBuilder.userDefaultOpts.idealPrepTime),
                n -> b.setIdealPrepTime(Integer.parseInt(n)));

        String m1 = "Kolik chcete nechat náhradních termínů?";
        return getBasicClassOptions(b, m1, "Jaká má být váha tohoto předmětu?");
    }

    private static ClassOptions getBasicClassOptions(ClassOptions.Builder b, String m1, String m2) {
        ask(m1, String.valueOf(configBuilder.userDefaultOpts.backupTries), n -> b.setBackupTries(Integer.parseInt(n)));
        ask(m2, String.valueOf(configBuilder.userDefaultOpts.weight), n -> b.setWeight(Integer.parseInt(n)));

        return b.createClassOptions();
    }

    private static LocalDate getDate() {
        msg("Zadejte prosím datum ve formátu \"den. měsíc. [rok]?\".");
        Optional<LocalDate> result = Utils.parseDate(sc.nextLine(), defaultYear);
        while (result.isEmpty()) {
            msg("Zadejte prosím datum ve formátu \"den. měsíc. [rok]?\".");
            result = Utils.parseDate(sc.nextLine(), defaultYear);
        }
        newline();
        return result.get();
    }

    private static WeightsConfig getWeightsConfig() {
        WeightsConfig.Builder b = new WeightsConfig.Builder();
        msg("Předmětům je přidělován čas na přípravu úměrně k jejich důležitosti.",
                "Důležitost předmětu je vypočítána jako: v * váha + s * st(status) + k * kredity,",
                "kde váha, status (P/PVP/V) a kredity jsou vlastnosti předmětu a v, s, st, k jsou,",
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

        msg("Nyní nastavíme funkci st, která přiřazuje číslo statusu předmětu (P/PVP/V).");

        String fmt2 = "Zadejte prosím hodnotu pro %s předměty.";

        StatusFunction st = new StatusFunction(0, 0, 0);
        ask(String.format(fmt2, "povinné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.P)),
                n -> st.p = Integer.parseInt(n));
        ask(String.format(fmt2, "povinně volitelné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.PVP)),
                n -> st.pvp = Integer.parseInt(n));
        ask(String.format(fmt2, "volitelné"),
                String.valueOf(Config.defaultWeightsConfig.st.apply(Status.V)),
                n -> st.v = Integer.parseInt(n));

        b.setStatusFunction(st);
        return b.createWeightsConfig();
    }

    private static Map<ClassInfo, Set<LocalDate>> getClassExamDates() {
        header("Úvodní nastavení");

        msg("Stáhněte si prosím ze SISu xlsx tabulku s termíny zkoušek (návod můžete najít v README).",
                "Až ji budete mít staženou, zadejte prosím cestu k ní:");
        String path = sc.nextLine();
        var result = ClassParser.parse(path);

        while (result.isEmpty()) {
            msg("Někde se stala chyba. Překontrolujte prosím cestu a zadejte ji znovu.");
            path = sc.nextLine();
            result = ClassParser.parse(path);
        }

        newline();

        return result.get();
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
