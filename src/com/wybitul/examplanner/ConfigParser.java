package com.wybitul.examplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private static PeekScanner sc;

    private ConfigParser() { }

    public static Optional<Config> parse(String path) {
        Config.Builder configBuilder = new Config.Builder();

        try {
            sc = new PeekScanner(new File(path));

            // Reading the header
            WeightsConfig.Builder wb = new WeightsConfig.Builder(Config.defaultWeightsConfig);
            while (sc.peekNextLine() != null && !sc.peekNextLine().startsWith("+++")) {
                String line = sc.nextLine();
                if (line.startsWith("-")) { wb.parse(line); }
            }

            // Reading the body
            ClassOptions.Builder currentClassBuilder = new ClassOptions.Builder(Config.defaultClassOptions);
            while (sc.hasNextLine()) {
                ClassOptions classOpts = parseClassOptions(currentClassBuilder);
                configBuilder.addClassOptions(classOpts);

                if (sc.peekNextLine() != null && !sc.peekNextLine().startsWith("==")) {
                    currentClassBuilder = new ClassOptions.Builder(parseClass(sc.nextLine()));
                }
            }

            return Optional.of(configBuilder.createConfig());
        } catch (IncorrectConfigFileException e) {
            System.out.printf("Error in configuration file on line %d\n", sc.getLineNumber());
        } catch (FileNotFoundException e) {
            System.out.printf("Can't find the file %s\n", path);
        }
        return Optional.empty();
    }

    private static ClassOptions parseClassOptions(ClassOptions.Builder b) throws IncorrectConfigFileException {
        while (sc.peekNextLine() != null && !sc.peekNextLine().startsWith("==")) {
            String line = sc.nextLine();
            if (line.startsWith("-")) { b.parse(line); }
        }
        return b.createClassOptions();
    }

    private static ClassInfo parseClass(String line) throws IncorrectConfigFileException {
        Pattern p = Pattern.compile("^==\\s+(.*)\\s+\\((.*)\\)\\s*$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return new ClassInfo(new ID(m.group(2)), m.group(1), Type.EXAM);
        } else {
            throw new IncorrectConfigFileException();
        }
    }
}
