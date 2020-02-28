package com.wybitul.examplanner;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T thing) throws IncorrectConfigFileException;
}
