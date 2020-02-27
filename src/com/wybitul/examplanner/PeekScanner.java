package com.wybitul.examplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PeekScanner {
    private String nextLine;
    private Scanner sc;
    private int lineNumber = 0;

    public PeekScanner(File file) throws FileNotFoundException {
        sc = new Scanner(file);
        nextLine = sc.nextLine();
    }

    public boolean hasNextLine() {
        return nextLine != null;
    }

    public String nextLine() {
        String temp = nextLine;
        nextLine = sc.nextLine();
        lineNumber += 1;
        return temp;
    }

    public String peekNextLine() {
        return nextLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
