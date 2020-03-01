package com.wybitul.examplanner;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Parses exam dates from an xlsx file and matches them to ClassInfo objects.
No more details can be retrieved from the table than the dates and ClassInfo.
 */

public class ClassParser {
    private static final Map<ClassInfo, Set<LocalDate>> dates = new HashMap<>();

    private static final Pattern namePattern = Pattern.compile("^(.*) \\((.*)\\)$");
    private static final Pattern typePattern = Pattern.compile("^(zápočet/kolokvium|zkouška)$");

    private ClassParser() { }

    public static Optional<Map<ClassInfo, Set<LocalDate>>> parse(String path) {
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
        if (row.getRowNum() == 0) { return; }

        DataFormatter formatter = new DataFormatter();
        String rawName = formatter.formatCellValue(row.getCell(4));
        String rawType = formatter.formatCellValue(row.getCell(5));
        String rawDate = formatter.formatCellValue(row.getCell(8));
        Matcher nameMatcher = namePattern.matcher(rawName);
        Matcher typeMatcher = typePattern.matcher(rawType);

        if (!nameMatcher.matches() || !typeMatcher.matches()) {
            System.out.printf("Encountered a problem when reading row %d\n", row.getRowNum() + 1);
            return;
        }

        String id = nameMatcher.group(2);
        Type type = rawType.equals("zkouška") ? Type.EXAM : Type.COLLOQUIUM;
        String name = type == Type.COLLOQUIUM ? nameMatcher.group(1) + " [zápočet]" : nameMatcher.group(1);

        var key = new ClassInfo(new ID(id), name, type);
        var examDates = dates.getOrDefault(key, new HashSet<>());
        dates.putIfAbsent(key, examDates);

        Utils.parseDate(rawDate, -1).ifPresentOrElse(
                examDates::add,
                () -> System.out.printf("Encountered a problem while reading row %d\n", row.getRowNum() + 1)
        );
    }
}
