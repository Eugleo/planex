package com.wybitul.planex.config;

import java.util.Objects;

public class ID {
    public final String str;

    public ID(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return Objects.equals(str, id.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str);
    }
}
