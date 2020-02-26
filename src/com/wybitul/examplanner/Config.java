package com.wybitul.examplanner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config implements HasValidation {
    String excelFilePath;
    LocalDate firstDate;
    Map<String, ClassParams> classParams = new HashMap<>();
    WeightConfigurator weightConfigurator = new WeightConfigurator();

    @Override
    public void validate() throws MissingFieldException {
        if (excelFilePath == null) {
            throw new MissingFieldException("Missing path to excel file with exam dates. Set with \"- tabulka: path/to/file.xlsx\".");
        }

        if (firstDate == null) {
            throw new MissingFieldException("Missing the date on which you begin to learn. Set with \"- začátek: DAY. MONTH. YEAR\".");
        }

        if (weightConfigurator.typeToInt == null) {
            throw new MissingFieldException("Missing the function which maps class status (P/PVP/V) to a number. Set with \"- st: #P, #PVP, #V\".");
        }
    }

    static class Builder extends OptionBuilder<Config> {
        public Builder() {
            super(new Config());

            addOption("tabulka", (value, c) -> c.excelFilePath = value);

            addOption("začátek", (value, c) -> {
                Pattern p = Pattern.compile("^(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{4})");
                Matcher m = p.matcher(value);
                m.find();

                int day = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                c.firstDate = LocalDate.of(year, month, day);
            });

            addOption("v", (value, c) -> c.weightConfigurator.weight = Integer.parseInt(value));

            addOption("k", (value, c) -> c.weightConfigurator.credits = Integer.parseInt(value));

            addOption("s", (value, c) -> c.weightConfigurator.type = Integer.parseInt(value));

            addOption("st", (value, c) -> {
                List<Integer> values = Arrays.stream(value.split(",\\s*"))
                        .map(s -> Integer.parseInt(s))
                        .collect(Collectors.toList());
                int v1 = values.get(0);
                int v2 = values.get(1);
                int v3 = values.get(2);
                c.weightConfigurator.typeToInt = type -> {
                    switch (type) {
                        case P:
                            return v1;
                        case PVP:
                            return v2;
                        default:
                            return v3;
                    }
                };
            });
        }

        public Builder addClassParams(String key, ClassParams cp) {
            object.classParams.put(key, cp);
            return this;
        }
    }
}
