package com.wybitul.examplanner;

import java.util.Objects;

enum Type {
    EXAM, COLLOQUIUM
}

public class ClassInfo {
    final ID id;
    final String name;
    Type type;

    public ClassInfo(ID id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassInfo classInfo = (ClassInfo) o;
        return id.equals(classInfo.id) && type == classInfo.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
