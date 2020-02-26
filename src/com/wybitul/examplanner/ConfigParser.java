package com.wybitul.examplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private ConfigParser() { }

    public static Config parse(String path) throws MissingFieldException, IncorrectConfigFileException, FileNotFoundException {
        Config.Builder configBuilder = new Config.Builder();
        int lineNumber = 0;
        try {
            Scanner sc = new Scanner(new File(path));

            // Reading the header
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;
                if (line.startsWith("-")) {
                    configBuilder.parse(line);
                } else if (line.startsWith("+++")) {
                    // End of header
                    break;
                }
            }

            String currentClass = "default";
            ClassParams defaultClassParams = new ClassParams();
            ClassParams.Builder classParamsBuilder = new ClassParams.Builder(defaultClassParams);

            // Reading the body
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;
                if (line.startsWith("==")) {
                    ClassParams cp = classParamsBuilder.build();
                    // Add "...zp" to ids of colloquia
                    configBuilder.addClassParams(cp.isColloquium ? currentClass + "zp" : currentClass, cp);
                    currentClass = parseCurrentClass(line);
                    classParamsBuilder = new ClassParams.Builder(new ClassParams(defaultClassParams));
                } else if (line.startsWith("-")) {
                    classParamsBuilder.parse(line);
                }
            }
            // Add the last class
            ClassParams cp = classParamsBuilder.build();
            configBuilder.addClassParams(cp.isColloquium ? currentClass + "zp" : currentClass, cp);
        } catch (MissingFieldException e) {
            System.out.printf("%s near line %d\n", e.getMessage(), lineNumber);
            throw e;
        } catch (IncorrectConfigFileException e) {
            System.out.printf("Error in configuration file on line %d\n", lineNumber);
            throw e;
        } catch (FileNotFoundException e) {
            System.out.printf("Can't find the file %s\n", path);
            throw e;
        }
        return configBuilder.build();
    }

    private static String parseCurrentClass(String line) throws IncorrectConfigFileException {
        Pattern p = Pattern.compile("^==\\s+.*\\s+\\((.*)\\)\\s*$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new IncorrectConfigFileException();
        }
    }
}
