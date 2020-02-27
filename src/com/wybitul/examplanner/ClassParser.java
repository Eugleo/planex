package com.wybitul.examplanner;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassParser {
    static boolean debug = false;
    private static Map<ClassInfo, List<LocalDate>> dates = new HashMap<>();

    private static Pattern namePattern = Pattern.compile("^(.*) \\((.*)\\)$");
    private static Pattern typePattern = Pattern.compile("^(zápočet/kolokvium|zkouška)$");
    private static Pattern datePattern = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}) - .*$");

    private ClassParser() { }

    public static Optional<Map<ClassInfo, List<LocalDate>>> parse(String path) {
        try {
            Workbook workbook = WorkbookFactory.create(new File(path));
            Sheet sheet = workbook.getSheetAt(0);
            sheet.forEach(ClassParser::parseRow);
            return Optional.of(dates);
        } catch (InvalidFormatException e) {
            System.out.println("The file should be in .xls or .xlsx format.");
        } catch (IOException e) {
            System.out.printf("Can't open the file %s.\n", path);
        }
        return Optional.empty();
    }

    private static void parseRow(Row row) {
        if (row.getRowNum() == 0) {
            return;
        }

        DataFormatter formatter = new DataFormatter();
        String rawName = formatter.formatCellValue(row.getCell(4));
        String rawType = formatter.formatCellValue(row.getCell(5));
        String rawDate = formatter.formatCellValue(row.getCell(8));
        Matcher nameMatcher = namePattern.matcher(rawName);
        Matcher typeMatcher = typePattern.matcher(rawType);
        Matcher dateMatcher = datePattern.matcher(rawDate);

        if (!nameMatcher.matches() || !typeMatcher.matches() || !dateMatcher.matches()) {
            if (debug) {
                System.out.printf("Encountered a problem when reading row %d\n", row.getRowNum() + 1);
            }
            return;
        }

        String id = nameMatcher.group(2);
        Type type = rawType.equals("zkouška") ? Type.EXAM : Type.COLLOQUIUM;
        String name = type == Type.COLLOQUIUM ? nameMatcher.group(1) : nameMatcher.group(1) + " (zápočet)";

        var key = new ClassInfo(new ID(id), name, type);
        var examDates = dates.getOrDefault(key, new ArrayList<>());
        dates.putIfAbsent(key, examDates);

        int year = Integer.parseInt(dateMatcher.group(3));
        int month = Integer.parseInt(dateMatcher.group(2));
        int day = Integer.parseInt(dateMatcher.group(1));
        LocalDate date = LocalDate.of(year, month, day);

        examDates.add(date);
    }
}
