package com.wybitul.planex;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Parses the configuration .plx file.
 */

public class ConfigParser {
    private static PeekScanner sc;

    private ConfigParser() {
    }

    public static Optional<Config> parse(String path) {
        Config.Builder configBuilder = new Config.Builder();

        try {
            sc = new PeekScanner(new File(path));
            int defaultYear = -1;

            // Reading the weight config
            WeightsConfig.Builder wb = new WeightsConfig.Builder();
            while (sc.peekNextLine() != null && !sc.peekNextLine().startsWith("+++")) {
                String line = sc.nextLine();
                if (line.startsWith("- rok")) {
                    Pattern p = Pattern.compile("^- rok:\\s*(\\d{4})");
                    Matcher m = p.matcher(line);

                    if (!m.find()) {
                        throw new IncorrectConfigFileException("Incorrect date specification");
                    }

                    defaultYear = Integer.parseInt(m.group(1));
                    configBuilder.defaultYear = defaultYear;
                } else if (line.startsWith("- začátek")) {
                    Pattern p = Pattern.compile("^- začátek:\\s*(.*)\\s*$");
                    Matcher m = p.matcher(line);

                    if (!m.find()) {
                        throw new IncorrectConfigFileException("Incorrect date format");
                    }

                    LocalDate date = Utils.parseDate(m.group(1), defaultYear)
                            .orElseThrow(() -> new IncorrectConfigFileException("Incorrect date format"));
                    configBuilder.setBeginning(date);
                } else if (line.startsWith("-")) {
                    wb.parse(line);
                }
            }
            configBuilder.setWeightsConfig(wb.createWeightsConfig());

            // Reading the body
            Set<ClassOptions> classOptionsSet = new HashSet<>();
            boolean buildingDefault = true;
            var currentClassBuilder = new ClassOptions.Builder(Config.defaultClassOptions, defaultYear);
            while (sc.hasNextLine()) {
                ClassOptions classOpts = parseClassOptions(currentClassBuilder);
                if (buildingDefault) {
                    buildingDefault = false;
                    configBuilder.setGlobalClassOptions(classOpts);
                } else {
                    classOptionsSet.add(classOpts);
                }

                if (sc.peekNextLine() != null && sc.peekNextLine().startsWith("=")) {
                    currentClassBuilder = new ClassOptions.Builder(
                            parseClass(sc.nextLine()),
                            configBuilder.globalClassOptions,
                            defaultYear
                    );
                }
            }

            return Optional.of(configBuilder.setClassOptions(classOptionsSet).createConfig());
        } catch (IncorrectConfigFileException e) {
            System.out.printf("%s on line %d\n", e.getMessage(), sc.getLineNumber());
        } catch (FileNotFoundException e) {
            System.out.printf("Can't find the file %s\n", path);
        }
        return Optional.empty();
    }

    private static ClassOptions parseClassOptions(ClassOptions.Builder b) throws IncorrectConfigFileException {
        while (sc.peekNextLine() != null && !sc.peekNextLine().startsWith("=")) {
            String line = sc.nextLine();
            if (line.startsWith("-")) {
                b.parse(line);
            }
        }
        return b.createClassOptions();
    }

    private static ClassInfo parseClass(String line) throws IncorrectConfigFileException {
        Pattern p = Pattern.compile("^=\\s+(.*)\\s+\\((.*)\\)\\s*$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return new ClassInfo(new ID(m.group(2)), m.group(1), Type.EXAM);
        } else {
            throw new IncorrectConfigFileException("Incorrectly specified class name");
        }
    }
}
