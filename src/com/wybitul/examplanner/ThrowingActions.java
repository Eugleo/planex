package com.wybitul.examplanner;

@FunctionalInterface
interface ThrowingRunnable {
    void run() throws IncorrectConfigFileException;
}

@FunctionalInterface
interface ThrowingConsumer<T> {
    void accept(T thing) throws IncorrectConfigFileException;
}
