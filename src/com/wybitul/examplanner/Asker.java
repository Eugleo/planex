package com.wybitul.examplanner;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "SameParameterValue"})
public class Asker {
    private static final Scanner sc = new Scanner(System.in);

    // `ask` where the default value is Optional.empty()
    // i.e. the caller will know that the user selected a default value, although the def value is actually empty
    static <T> Optional<T> ask(String spec, Function<String, Optional<T>> trans, String defString) {
        return ask(spec, trans, Optional::isPresent, Optional.empty(), defString);
    }

    // `ask` with a default value, which has an automatically generated description
    static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred, T def) {
        return ask(spec, trans, pred, def, String.valueOf(def));
    }

    // `ask` with the predicate automatically set to Optional::isPresent, because `trans` returns optional,
    // and a default value if the user enters ""
    static <T> T ask(String spec, Function<String, Optional<T>> trans, T def, String defStr) {
        return ask(spec, trans, Optional::isPresent, Optional.of(def), defStr).get();
    }

    // `ask` with the predicate automatically set to Optional::isPresent, because `trans` returns optional
    static <T> T ask(String spec, Function<String, Optional<T>> trans) {
        return ask(spec, trans, Optional::isPresent).get();
    }

    // `ask` with a default value if user enters ""
    // ADAM Jak správně formátovat víceřádkovou deklaraci?
    static <T> T ask(String spec, Function<String, T> trans,
                     Predicate<T> pred, T def, String defString) {
        Optional<T> result = ask(
                String.format("%s, nebo pro zachování výchozí hodnoty (%s) nechte vstup prázdný", spec, defString),
                s -> s.isEmpty() ? Optional.empty() : Optional.of(trans.apply(s)),
                t -> t.isEmpty() || pred.test(t.get()),
                false
        );

        if (result.isEmpty()) { msg(String.format("(nastaveno: %s)", defString)); }
        newline();

        return result.orElse(def);
    }

    // `ask` with newline after it
    static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred) {
        return ask(spec, trans, pred, true);
    }

    // Ask the user to enter information according to `spec`, transform in to T with `trans`,
    // and check it with the predicate `pred`
    // If `trans` fails or `pred` returns false, ask the user to enter the information again
    private static <T> T ask(String spec, Function<String, T> trans, Predicate<T> pred, boolean newline) {
        T tInput = null;
        String input;
        boolean test = false;

        msg(String.format("[%s]", spec));
        while (tInput == null || !test) {
            input = sc.nextLine();
            try {
                tInput = trans.apply(input);
                test = pred.test(tInput);
                if (!test) { msg(String.format("Nesprávný vstup, %s.", spec)); }
            } catch (Exception e) {
                msg(String.format("Nesprávný vstup, %s.", spec));
            }
        }

        if (newline) { newline(); }
        return tInput;
    }

    static void msg(String... messages) {
        System.out.println(String.join(" ", messages));
    }

    static void newline() { msg(); }

    static void section(String... messages) {
        String text = String.join(" ", messages);
        msg(text);
        msg("=".repeat(text.length()));
        newline();
    }

    static void header(String... messages) {
        String text = String.join(" ", messages);
        msg(text);
        msg("-".repeat(text.length()));
    }
}
