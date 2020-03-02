package com.wybitul.planex;

public enum Status {
    P, PVP, V;

    @Override
    public String toString() {
        switch (this) {
            case P:
                return "povinný";
            case PVP:
                return "povinně volitelný";
            default:
                return "volitelný";
        }
    }
}