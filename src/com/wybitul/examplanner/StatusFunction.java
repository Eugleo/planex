package com.wybitul.examplanner;

public class StatusFunction {
    int p;
    int pvp;
    int v;

    public StatusFunction(int p, int pvp, int v) {
        this.p = p;
        this.pvp = pvp;
        this.v = v;
    }

    public int apply(Status s) {
        switch (s) {
            case P:
                return p;
            case PVP:
                return pvp;
            default:
                return v;
        }
    }
}
