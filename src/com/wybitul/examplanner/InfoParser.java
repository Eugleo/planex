package com.wybitul.examplanner;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoParser {
    static boolean debug = false;

    private InfoParser() { }

    public static Map<String, Info> parse(String path) {
        Map<String, Info> result = new HashMap<>();
        try {
            Workbook workbook = WorkbookFactory.create(new File(path));
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Pattern namePattern = Pattern.compile("^(.*) \\((.*)\\)$");
            Pattern typePattern = Pattern.compile("^(zápočet/kolokvium|zkouška)$");
            Pattern datePattern = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}) - .*$");

            for (Row row: sheet) {
                if (row.getRowNum() == 0) { continue; }

                String rawName = formatter.formatCellValue(row.getCell(4));
                String rawType = formatter.formatCellValue(row.getCell(5));
                String rawDate = formatter.formatCellValue(row.getCell(8));

                Matcher nameMatcher = namePattern.matcher(rawName);
                Matcher typeMatcher = typePattern.matcher(rawType);
                Matcher dateMatcher = datePattern.matcher(rawDate);

                if (!nameMatcher.matches() || !typeMatcher.matches() || !dateMatcher.matches()) {
                    if (debug) {
                        System.out.printf("Ecountered a problem when reading row %d\n", row.getRowNum() + 1);
                    }
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

                Info info = result.getOrDefault(id, new Info(name));
                info.exams.add(exam);
                result.putIfAbsent(id, info);
            }
        } catch (InvalidFormatException e) {
            System.out.println("The file should be in .xls or .xlsx format");
        } catch (IOException e) {
            System.out.printf("Can't open the file %s\n", path);
        } finally {
            return result;
        }
    }
}
