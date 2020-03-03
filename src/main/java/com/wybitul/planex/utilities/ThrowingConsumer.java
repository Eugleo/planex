package com.wybitul.planex.utilities;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T thing) throws IncorrectConfigFileException;
}
