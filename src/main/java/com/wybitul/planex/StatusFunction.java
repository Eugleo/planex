package com.wybitul.planex;

public class StatusFunction {
    final int p;
    final int pvp;
    final int v;

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
