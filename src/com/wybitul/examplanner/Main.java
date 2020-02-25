package com.wybitul.examplanner;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) throws Exception {
        var w = new WeightConfigurator(t -> 5, 1, 1, 1);
        var firstDate =  LocalDate.of(2020, 01, 01);

        try {
            Workbook workbook = WorkbookFactory.create(new File(args[0]));
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Pattern namePattern = Pattern.compile("^(.*) \\((.*)\\)$");
            Pattern typePattern = Pattern.compile("^(zápočet/kolokvium|zkouška)$");
            Pattern datePattern = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}) - .*$");

            Map<String, List<Exam>> classExams = new HashMap<>();
            Map<String, ClassConfig> classConfigs = new HashMap<>();
            classConfigs.put("NPRG013",
                    new ClassConfig("Java", ClassType.PVP, 3, 5, 6));
            classConfigs.put("MB140P17",
                    new ClassConfig("Genetika", ClassType.P, 14, 5, 5));
            classConfigs.put("MB150P14E",
                    new ClassConfig("Immunology", ClassType.PVP, 7, 5, 3));
            classConfigs.put("MB150P34",
                    new ClassConfig("Základy biochemie", ClassType.P, 10, 5, 3));
            classConfigs.put("MB170P55",
                    new ClassConfig("Úvod do evoluční biologie", ClassType.P, 5, 5, 3));
            classConfigs.put("MS107011",
                    new ClassConfig("Teorie her a evoluce", ClassType.V, 0, 5, 2));
            classConfigs.put("NPFL129",
                    new ClassConfig("Strojové učení pro zelenáče", ClassType.V, 9, 5, 5));
            classConfigs.put("NPRG068",
                    new ClassConfig("Programování v Haskellu", ClassType.V, 1, 5, 3));
            classConfigs.put("NTIN061",
                    new ClassConfig("Algoritmy a datové struktury II", ClassType.P, 7, 5, 6));

            for (Row row: sheet) {
                if (row.getRowNum() == 0) { continue; }

                String rawName = formatter.formatCellValue(row.getCell(4));
                String rawType = formatter.formatCellValue(row.getCell(5));
                String rawDate = formatter.formatCellValue(row.getCell(8));

                Matcher nameMatcher = namePattern.matcher(rawName);
                Matcher typeMatcher = typePattern.matcher(rawType);
                Matcher dateMatcher = datePattern.matcher(rawDate);

                if (!nameMatcher.matches() || !typeMatcher.matches() || !dateMatcher.matches()) {
                    System.out.printf("Ecountered a problem when reading row %d\n", row.getRowNum() + 1);
                    continue;
                }

                String name = nameMatcher.group(1);
                String id = nameMatcher.group(2);
                ExamType examType = rawType.equals("zkouška") ? ExamType.Exam : ExamType.Colloquium;
                int year = Integer.parseInt(dateMatcher.group(3));
                int month = Integer.parseInt(dateMatcher.group(2));
                int day = Integer.parseInt(dateMatcher.group(1));
                LocalDate date = LocalDate.of(year, month, day);
                Exam exam = new Exam(date, examType);

                List<Exam> exams = classExams.getOrDefault(id, new ArrayList<>());
                exams.add(exam);
                classExams.putIfAbsent(id, exams);
            }

            List<UniversityClass> classes = new ArrayList<>();
            classExams.forEach((key, exams) -> classes.add(new UniversityClass(key, classConfigs.get(key), exams)));

            Model model = new Model(classes, firstDate, w);
            Solver solver = new Solver(model, firstDate);
            List<Result> results = solver.solve();
            results.stream()
                    .sorted(Comparator.comparing(r -> r.exam.date))
                    .forEach(r -> System.out.printf("%s: %s\n", r.uniClass.name, r.exam.date));
        } catch (InvalidFormatException e) {
            System.out.println("The file should be in .xls or .xlsx format");
        } catch (IOException e) {
            System.out.printf("Can't open the file %s\n", args[0]);
        }
    }
}
