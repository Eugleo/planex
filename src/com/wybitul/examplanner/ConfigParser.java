package com.wybitul.examplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private ConfigParser() { }

    public static Config parse(String path) throws MissingFieldException {
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
                } else {
                    continue;
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
                    configBuilder.addClassParams(currentClass, classParamsBuilder.build());
                    currentClass = parseCurrentClass(line);
                    classParamsBuilder = new ClassParams.Builder(new ClassParams(defaultClassParams));
                } else if (line.startsWith("-")) {
                    classParamsBuilder.parse(line);
                } else {
                    continue;
                }
            }
        } catch (MissingFieldException e) {
            System.out.printf("%s near line %d\n", e.getMessage(), lineNumber);
        } catch (IncorrectConfigFileException e) {
            System.out.printf("Error in configuration file on line %d\n", lineNumber);
        } catch (FileNotFoundException e) {
            System.out.printf("Can't find the file %s\n", path);
        } catch (Exception e) {
            /*
            ADAM
            Myslel jsem, že když tento catch vynechám, jakýkoli nematchnutý Exception (i.e. cokoli, co není
            MissingFieldException, IncorrectConfigFileException, FileNotFoundException) se vyhodí, ale není to tak?
            */
            throw e;
        } finally {
            return configBuilder.build();
        }
    }

    private static String parseCurrentClass(String line) throws IncorrectConfigFileException {
        try {
            Pattern p = Pattern.compile("^==\\s+.*\\s+\\((.*)\\)\\s*$");
            Matcher m = p.matcher(line);
            m.find();
            return m.group(1);
        } catch (Exception e) {
            throw new IncorrectConfigFileException();
        }
    }
}
