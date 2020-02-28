package com.wybitul.examplanner;

class ModelException extends Exception {
    ModelException(String message) {
        super(message);
    }
}

class IncorrectConfigFileException extends Exception {
    IncorrectConfigFileException(String message) {
        super(message);
    }
}