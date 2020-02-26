package com.wybitul.examplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private ConfigParser() { }

    public static Map<String, Config> parse(String path) {
        Map<String, Config> result = new HashMap<>();
        int lineNumber = 0;
        try {
            Scanner sc = new Scanner(new File(path));
            Config defaultConfig = new Config();
            String currentClass = null;
            Config currentConfig = null;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;
                if (line.startsWith("==")) {
                    if (currentClass != null) {
                        result.put(currentClass, currentConfig);
                    }
                    currentClass = parseCurrentClass(line);
                    currentConfig = new Config();
                } else if (line.startsWith("-")) {
                    if (currentClass == null) {
                        editConfig(defaultConfig, line);
                    } else {
                        editConfig(currentConfig, line);
                    }
                } else {
                    continue;
                }
            }

            // Fill in the defaults if a value is missing
            result.forEach((key, config) -> {
                if (config.highBound == null && defaultConfig.highBound != null) {
                    config.highBound = defaultConfig.highBound;
                }
                if (config.lowBound == null && defaultConfig.lowBound != null) {
                    config.lowBound = defaultConfig.lowBound;
                }
                if (config.idealPrepTime == -1) {
                    if (defaultConfig.idealPrepTime != -1) {
                        config.idealPrepTime = defaultConfig.idealPrepTime;
                    } else {
                        config.idealPrepTime = 1;
                    }
                }
                if (config.weight == -1) {
                    if (defaultConfig.weight != -1) {
                        config.weight = defaultConfig.weight;
                    } else {
                        config.weight = 0;
                    }
                }
                if (config.credits == -1) {
                    if (defaultConfig.credits != -1) {
                        config.credits = defaultConfig.credits;
                    } else {
                        config.credits = 0;
                    }
                }
                if (config.type == null) {
                    if (defaultConfig.type != null) {
                        config.type = defaultConfig.type;
                    } else {
                        config.type = ClassType.V;
                    }
                }
            });
        } catch (IncorrectConfigException e) {
            System.out.printf("Error in configuration file on line %d\n", lineNumber);
        } catch (FileNotFoundException e) {
            System.out.printf("Can't find the file %s\n", path);
        } finally {
            return result;
        }
    }

    private static String parseCurrentClass(String line) throws IncorrectConfigException {
        try {
            Pattern p = Pattern.compile("^==\\s+.*\\s+\\((.*)\\)\\s*$");
            Matcher m = p.matcher(line);
            m.find();
            return m.group(1);
        } catch (Exception e) {
            throw new IncorrectConfigException();
        }
    }

    private static void editConfig(Config c, String line) throws IncorrectConfigException {
        try {
            Pattern p = Pattern.compile("^-\\s+(.*)\\s*:\\s+(.+)\\s*$");
            Matcher m = p.matcher(line);
            m.find();

            switch (m.group(1)) {
                case "omezení":
                    Pattern pa = Pattern.compile("((\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})|.)\\s*-\\s*((\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})|.)");
                    Matcher ma = pa.matcher(m.group(2));
                    ma.find();
                    if (!ma.group(1).matches("^.$")) {
                        String days = ma.group(2);
                        int day = Integer.parseInt(ma.group(2));
                        int month = Integer.parseInt(ma.group(3));
                        int year = Integer.parseInt(ma.group(4));
                        c.lowBound = LocalDate.of(year, month, day);
                    } else if (!ma.group(5).matches("^.$")) {
                        int day = Integer.parseInt(ma.group(6));
                        int month = Integer.parseInt(ma.group(7));
                        int year = Integer.parseInt(ma.group(8));
                        c.highBound = LocalDate.of(year, month, day);
                    }
                    break;
                case "příprava":
                    Pattern pat = Pattern.compile("^(\\d+)");
                    Matcher mat = pat.matcher(m.group(2));
                    mat.find();
                    c.idealPrepTime = Integer.parseInt(mat.group(1));
                    break;
                case "kredity":
                    c.credits = Integer.parseInt(m.group(2));
                    break;
                case "váha":
                case "důležitost":
                    c.weight = Integer.parseInt(m.group(2));
                case "status":
                    switch (m.group(2)) {
                        case "P":
                            c.type = ClassType.P;
                            break;
                        case "PVP":
                            c.type = ClassType.PVP;
                            break;
                        case "V":
                            c.type = ClassType.V;
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            throw new IncorrectConfigException();
        }
    }
}
