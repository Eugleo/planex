package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InteractiveConfigurator {
    private final Config.Builder configBuilder;
    private int defaultYear = -1;
    private int detailLevel;

    private final Set<ID> creditsDownloaded = new HashSet<>();
    private final Set<ID> statusDownloaded = new HashSet<>();

    public InteractiveConfigurator(Config.Builder builder) {
        this.configBuilder = builder;
    }

    // ADAM Kdybych chtěl ty zprávy ve více jazycích, musel bych je mít uložené v nějakém objektu
    // a používat je jako msg(o.welcome), msg(o.settings) atd, nebo je nějaký lepší způsob?
    public Config startConfiguration() {
        Asker.section("Úvodní nastavení");

        detailLevel = getDetailLevel();
        Map<ClassInfo, Set<LocalDate>> classExamDates = getClassExamDates();

        Asker.section("Obecná nastavení");

        if (detailLevel > 0) {
            defaultYear = getDefaultYear();
        }

        Asker.msg("Od kdy se můžete začít učit?");
        configBuilder.setBeginning(getDate());

        if (detailLevel > 2) {
            configBuilder.setWeightsConfig(getWeightsConfig());
        }
        if (detailLevel > 0) {
            configBuilder.setGlobalClassOptions(getGlobalClassOptions());
        }

        Set<ClassOptions.Builder> classBuilders = classExamDates.keySet().stream()
                .map(c -> new ClassOptions.Builder(c, configBuilder.globalClassOptions, defaultYear))
                .collect(Collectors.toSet());

        Asker.section("Nastavení jednotlivých předmětů");

        classBuilders.forEach(b -> b.setExamDates(classExamDates.getOrDefault(b.classInfo, new HashSet<>())));
        optionallyParseCreditsAndStatus(classBuilders);
        Set<ClassOptions> classOptions = classBuilders.stream()
                .map(this::getClassOptions)
                .collect(Collectors.toSet());
        configBuilder.setClassOptions(classOptions);

        return configBuilder.createConfig();
    }

    private ClassOptions getClassOptions(ClassOptions.Builder b) {
        Asker.header("Nastavení předmětu", b.classInfo.name);

        Asker.msg("Kolik dní byste v ideálním případě chtěli mít na přípravu na tento předmět?");
        b.setIdealPrepTime(getPositiveNumber(configBuilder.globalClassOptions.idealPrepTime));

        if (!creditsDownloaded.contains(b.classInfo.id)) {
            Asker.msg("Kolik kreditů je za tento předmět?");
            b.setCredits(getPositiveNumber(b.credits));
        }

        if (!statusDownloaded.contains(b.classInfo.id)) {
            Asker.msg("Je tento předmět povinný, povinně volitelný nebo volitelný?");
            Status status = Asker.ask(
                    "Zadejte P, PVP nebo V.",
                    s -> Status.valueOf(s.toUpperCase()),
                    i -> true,
                    configBuilder.globalClassOptions.status
            );
            b.setStatus(status);
        }

        if (detailLevel == 0) { return b.createClassOptions(); }

        Asker.msg("Kolik chcete nechat náhradních termínů?");
        b.setBackupTries(getPositiveNumber(configBuilder.globalClassOptions.backupTries));

        if (detailLevel <= 1) { return b.createClassOptions(); }

        Asker.msg("Jakou váhu chcete přiřadit tomuto předmětu?");
        b.setWeight(getNumber(configBuilder.globalClassOptions.weight));

        if (detailLevel <= 2) { return b.createClassOptions(); }

        Asker.msg("Kdy nejdříve můžete dělat zkoušku z tohoto předmětu??");
        Optional<LocalDate> lowBound = Asker.ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                Optional::isPresent,
                configBuilder.globalClassOptions.lowBound,
                "bez omezení"
        );

        Asker.msg("Kdy nejpozději můžete dělat zkoušku z tohoto předmětu?");
        Optional<LocalDate> highBound = Asker.ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                Optional::isPresent,
                configBuilder.globalClassOptions.highBound,
                "bez omezení"
        );

        Asker.msg("Kolik nejméně dní potřebujete na přípravu? S tímto nastavením opatrně,",
                "jedná se o pevnou hranici a mohlo by se tedy stát, že za daných podmínek nebude existovat řešení.");
        b.setMinPrepTime(getPositiveNumber(configBuilder.globalClassOptions.minPrepTime));

        Asker.msg("Přejete si, aby model tento předmět ignoroval?");
        Boolean shouldIgnore = Asker.ask(
                "odpovězte prosím \"a\" nebo \"n\"",
                Utils::parseBoolean,
                false,
                "neignorovat"
        );

        return b.setLowBound(lowBound).setHighBound(highBound).setIgnore(shouldIgnore).createClassOptions();
    }

    private Map<ClassInfo, Set<LocalDate>> getClassExamDates() {
        Asker.header("Nastavení termínů zkoušek");

        Asker.msg("Stáhněte si prosím ze SISu xlsx tabulku s termíny zkoušek (návod můžete najít v README).");
        return Asker.ask("zadejte prosím cestu k platnému xlsx souboru", ClassParser::parse);
    }

    private ClassOptions getGlobalClassOptions() {
        Asker.header("Nastavení globálních parametrů");

        Asker.msg("Nyní nastavíme globální parametry (pro každý předmět zvlášť pak půjdou přepsat).",
                "Pro detailní popis jednotlivých parametrů viz README.",
                "Pokročilé parametry mohou být nastaveny ručně v konfiguračním souboru.");

        var b = new ClassOptions.Builder(configBuilder.globalClassOptions, defaultYear);

        Asker.newline();

        Asker.msg("Kolik chcete nechat náhradních termínů?");
        b.setBackupTries(getPositiveNumber(configBuilder.globalClassOptions.backupTries));

        Asker.msg("Kdy nejdříve můžete dělat zkoušku?");
        Optional<LocalDate> lowBound = Asker.ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                "bez omezení"
        );
        Asker.msg("Kdy nejpozději můžete dělat zkoušku?");
        Optional<LocalDate> highBound = Asker.ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear),
                "bez omezení"
        );
        b.setLowBound(lowBound).setHighBound(highBound);

        if (detailLevel <= 1) { return b.createClassOptions(); }

        Asker.msg("Jaká chcete aby byla výchozí váha předmětu?");
        b.setWeight(getNumber(configBuilder.globalClassOptions.weight));

        if (detailLevel <= 2) { return b.createClassOptions(); }

        Asker.msg("Kolik nejméně dní (obecně) potřebujete na přípravu? S tímto nastavením opatrně,",
                "jedná se o pevnou hranici a mohlo by se tedy stát, že za daných podmínek nebude existovat řešení.");
        b.setMinPrepTime(getPositiveNumber(configBuilder.globalClassOptions.minPrepTime));

        return b.createClassOptions();
    }

    private int getDefaultYear() {
        Asker.msg("V průběhu konfigurace budete zadávat různá data.",
                "Abyste si ušetřili čas s jejich psaním, zadejte prosím výchozí rok",
                "který pak bude automaticky přidán ke každému datu bez specifikovaného roku.");

        return Asker.ask(
                "zadejte celé číslo mezi 2000 a 3000",
                Integer::parseInt,
                i -> 2000 <= i && i <= 3000
        );
    }

    private int getDetailLevel() {
        Asker.msg("Konfiguraci je možno provádět různě detailně",
                "podle toho, kolik s ní chcete strávit času a jak moc si ji chcete přizpůsobit.",
                "Nastavte prosím úroveň podrobností, na kterou se vás bude konfigurátor ptát",
                "(pro nové uživatele doporučujeme úroveň 1).");
        return Asker.ask(
                "zadejte číslo 0 (jen nejnutnější informace) až 3 (velké podrobnosti)",
                Integer::parseInt,
                n -> 0 <= n && n <= 3,
                1
        );
    }

    private void optionallyDownloadCredits(Set<ClassOptions.Builder> builders) {
        Asker.msg("Chcete alespoň nechat ze SISu stáhnout kreditové ohodnocení jednotlivých předmětů?");

        Boolean shouldDownload = Asker.ask(
                "odpovězte prosím \"a\" nebo \"n\"",
                Utils::parseBoolean
        );

        // Set credits to be equal to the downloaded ones
        if (shouldDownload) {
            List<ID> ids = builders.stream()
                    .map(b -> b.classInfo.id)
                    .collect(Collectors.toList());

            Asker.msg("Chvilku strpení, stahuji informaci o kreditech...");
            HashMap<ID, Integer> creditMap = CreditDownloader.getCredits(ids);
            builders.forEach(b -> {
                ID id = b.classInfo.id;
                if (creditMap.containsKey(id)) { b.setCredits(creditMap.get(id)); }
            });
            Asker.msg("Hotovo");
        }
        Asker.newline();
    }

    private void optionallyParseCreditsAndStatus(Set<ClassOptions.Builder> builders) {
        Asker.header("Automatické předvyplnění údajů");

        Asker.msg("Doporučujeme vám stáhnout stránku ze SISu nazvanou \"Zápis předmětů a rozvrhu\",",
                "s její pomocí totiž budeme schopní vyplnit spoustu informací o předmětech automaticky",
                "(jinak byste je museli vypisovat ručně).");

        Optional<CreditsAndStatusParser> parserOpt = Asker.ask(
                "zadejte cestu k platnému html souboru",
                CreditsAndStatusParser::makeParser,
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

    private LocalDate getDate() {
        return Asker.ask(
                "zadejte datum ve formátu: den. měsíc. (popř. rok)",
                s -> Utils.parseDate(s, defaultYear)
        );
    }

    private int getNumber(int def) {
        return Asker.ask(
                "zadejte prosím celé číslo",
                Integer::parseInt,
                i -> true,
                def
        );
    }

    private int getPositiveNumber(int def) {
        return Asker.ask(
                "zadejte prosím celé nezáporné číslo",
                Integer::parseInt,
                i -> i >= 0,
                def
        );
    }

    private WeightsConfig getWeightsConfig() {
        Asker.header("Nastavení výpočtu důležitosti předmětu");

        WeightsConfig.Builder b = new WeightsConfig.Builder();
        Asker.msg("Předmětům je přidělován čas na přípravu úměrně k jejich důležitosti.",
                "Důležitost předmětu je vypočítána jako: v * váha + s * st(status) + k * kredity,",
                "kde váha, status (P/PVP/V) a kredity jsou vlastnosti předmětu a v, s, st, k jsou,",
                "globální parametry.");

        Asker.newline();

        String fmt = "Jakou hodnotu chcete aby měl parametr u %s (%s)?";

        Asker.msg(String.format(fmt, "váhy", "v"));
        b.setWeight(getPositiveNumber(Config.defaultWeightsConfig.w));

        Asker.msg(String.format(fmt, "statusu", "s"));
        b.setStatus(getPositiveNumber(Config.defaultWeightsConfig.s));

        Asker.msg(String.format(fmt, "kreditů", "k"));
        b.setCredit(getPositiveNumber(Config.defaultWeightsConfig.c));

        Asker.header("Nastavení statusové funkce (st)");

        String fmt2 = "Zadejte prosím hodnotu pro %s předměty.";

        Asker.msg(String.format(fmt2, "povinné"));
        int p = getNumber(Config.defaultWeightsConfig.st.apply(Status.P));

        Asker.msg(String.format(fmt2, "povinně volitelné"));
        int pvp = getNumber(Config.defaultWeightsConfig.st.apply(Status.PVP));

        Asker.msg(String.format(fmt2, "volitelné"));
        int v = getNumber(Config.defaultWeightsConfig.st.apply(Status.V));

        return b.setStatusFunction(new StatusFunction(p, pvp, v)).createWeightsConfig();
    }
}
