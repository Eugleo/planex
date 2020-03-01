package com.wybitul.examplanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
Parses credits and statuses from a given .html file.
The file needs to be downloaded manually from Subjects and schedule registration on SIS.
 */

public class CreditsAndStatusParser {
    private Document doc;

    public Map<ID, Integer> getCreditsMap() {
        return creditsMap;
    }

    public Map<ID, Status> getStatusesMap() {
        return statusesMap;
    }

    private Map<ID, Integer> creditsMap;
    private Map<ID, Status> statusesMap;

    private CreditsAndStatusParser(String path) throws IOException {
        doc = Jsoup.parse(new File(path), "UTF-8");
        creditsMap = parseCredits();
        statusesMap = parseStatuses();
    }

    static Optional<CreditsAndStatusParser> makeParser(String path) {
        try {
            return Optional.of(new CreditsAndStatusParser(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Map<ID, Integer> parseCredits() {
        Elements winterCr = doc.select("#content > table.tab1 tr > td[nowrap]:nth-child(4)");
        Elements summerCr = doc.select("#content > table.tab1 tr > td[nowrap]:nth-child(5)");

        List<ID> classIDs = doc.select("#content > table.tab1 tr > td[align]:nth-child(8) > a")
                .stream()
                .map(e -> new ID(e.text().strip()))
                .collect(Collectors.toList());

        List<Integer> credits = IntStream.range(0, winterCr.size()).mapToObj(i -> {
            try {
                return Integer.parseInt(winterCr.get(i).text().strip());
            } catch (NumberFormatException e) {
                return Integer.parseInt(summerCr.get(i).text().strip());
            }
        }).collect(Collectors.toList());

        return IntStream.range(0, classIDs.size())
                .mapToObj(i -> Map.entry(classIDs.get(i), credits.get(i)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<ID, Status> parseStatuses() {
        List<ID> classIDs = doc.select("#content > table.tab1 tr > td[align]:nth-child(8) > a")
                .stream()
                .map(e -> new ID(e.text().strip()))
                .collect(Collectors.toList());

        List<Status> classStatuses = doc.select("#content > table.tab1 tr > td[nowrap]:nth-child(12)")
                .stream().map(e -> {
                    switch (e.text().strip()) {
                        case "povinný":
                            return Status.P;
                        case "povinně volitelný":
                            return Status.PVP;
                        default:
                            return Status.V;
                    }
                })
                .collect(Collectors.toList());

        return IntStream.range(0, classIDs.size())
                .mapToObj(i -> Map.entry(classIDs.get(i), classStatuses.get(i)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
