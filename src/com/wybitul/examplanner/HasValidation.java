package com.wybitul.examplanner;

class MissingFieldException extends Exception {
    public MissingFieldException(String message) {
        super(message);
    }
}

public interface HasValidation {
    void validate() throws MissingFieldException;
}
