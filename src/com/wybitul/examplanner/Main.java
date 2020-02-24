package com.wybitul.examplanner;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;

import java.io.File;
import java.io.IOException;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) {
        CpModel model = new CpModel();


        try {
            Workbook workbook = WorkbookFactory.create(new File(args[0]));
            Sheet sheet = workbook.getSheetAt(0);
        } catch (InvalidFormatException e) {
            System.out.println("The file should be in .xls or .xlsx format");
        } catch (IOException e) {
            System.out.printf("Can't open the file %s\n", args[0]);
        }
    }
}
