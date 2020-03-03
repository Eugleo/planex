package com.wybitul.planex.config.loading;

import com.wybitul.planex.config.ID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

/*
Automatically downloads credits from SIS based on their class IDs.
This information is available in SIS even without any authentication.
 */

public class CreditDownloader {
    private static final String pathToMap = "credits.map";
    private static final String pathToCredits = "#content table > tbody > tr > th:contains(E-kredity:) + td";
    private static final String sisURL = "https://is.cuni.cz/studium/predmety/index.php?do=predmet&kod=";
    private static HashMap<ID, Integer> creditMap;

    // TODO Fix warning
    static {
        try (FileInputStream fis = new FileInputStream(new File(pathToMap));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            creditMap = (HashMap<ID, Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            creditMap = new HashMap<>();
        }
    }

    private CreditDownloader() {
    }

    public static HashMap<ID, Integer> getCredits(Collection<ID> ids) {
        ids.stream()
                .filter(id -> !creditMap.containsKey(id))
                .forEach(id -> {
                    try {
                        Document doc = Jsoup.connect(sisURL + id.str).get();
                        Element e = doc.select(pathToCredits).first();
                        if (e != null) {
                            int credits = Integer.parseInt(e.text());
                            creditMap.put(id, credits);
                        } else {
                            System.out.println("Unable to download credits for class " + id.str);
                        }
                    } catch (NumberFormatException | IOException e) {
                        System.out.println("Unable to download credits for class " + id.str);
                    }
                });

        try (FileOutputStream fos = new FileOutputStream(pathToMap);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(creditMap);
        } catch (IOException ignored) {
        }

        return creditMap;
    }

    @SuppressWarnings("unused")
    public static boolean reset() {
        return new File(pathToMap).delete();
    }
}
