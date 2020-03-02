package com.wybitul.planex;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T thing) throws IncorrectConfigFileException;
}
