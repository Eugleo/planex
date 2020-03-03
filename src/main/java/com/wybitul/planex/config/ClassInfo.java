package com.wybitul.planex.config;

import java.util.Objects;

/*
Basic info about a class. You can discern one class (ClassOption) from another only by its ClassInfo.
 */

public class ClassInfo {
    public final ID id;
    public final String name;
    public Type type;

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
