package com.karamanolev.openscad.exporter;

import com.karamanolev.XY;

public class ScadHelper {
    public static ScadDoubleVector toDoubleVector(XY xy) {
        return new ScadDoubleVector(xy.getX(), xy.getY());
    }

    public static ScadVector toVector(XY[] xys) {
        ScadDoubleVector[] items = new ScadDoubleVector[xys.length];
        for (int i = 0; i < xys.length; i++) {
            items[i] = toDoubleVector(xys[i]);
        }
        return new ScadVector(items);
    }
}
