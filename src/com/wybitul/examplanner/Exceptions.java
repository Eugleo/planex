package com.wybitul.examplanner;

class ModelException extends Exception {
    ModelException(String message) {
        super(message);
    }
}

class IncorrectConfigFileException extends Exception { }

class MissingFieldException extends Exception { }
