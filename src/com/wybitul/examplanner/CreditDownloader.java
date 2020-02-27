package com.wybitul.examplanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;

public class CreditDownloader {
    private static HashMap<ID, Integer> creditMap;

    private static String pathToMap = "credits.map";
    private static String pathToCredits = "#content > div.form_div.pageBlock > table > tbody > tr >" +
            "td:nth-child(1) > table > tbody > tr:nth-child(6) > td";
    private static String sisURL = "https://is.cuni.cz/studium/predmety/index.php?do=predmet&kod=";

    static {
        try (FileInputStream fis = new FileInputStream(new File(pathToMap));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            creditMap = (HashMap<ID, Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            creditMap = new HashMap<>();
        }
    }

    private CreditDownloader() { }

    public static HashMap<ID, Integer> getCredits(Collection<ClassInfo> classInfo) {
        classInfo.stream()
                .map(c -> c.id)
                .filter(id -> !creditMap.containsKey(id))
                .forEach(id -> {
                    try {
                        Document doc = Jsoup.connect(sisURL + id).get();
                        Element e = doc.select(pathToCredits).first();
                        int credits = Integer.parseInt(e.text());
                        creditMap.put(id, credits);
                    }
                    catch (NumberFormatException | IOException ignored) { }
                });

        try (FileOutputStream fos = new FileOutputStream(new File(pathToMap));
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(creditMap);
        } catch (IOException ignored) { }

        return creditMap;
    }

    public static void reset() {
        new File(pathToMap).delete();
    }
}
