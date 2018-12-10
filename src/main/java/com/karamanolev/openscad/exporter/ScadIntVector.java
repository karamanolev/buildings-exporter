package com.karamanolev.openscad.exporter;

import java.util.ArrayList;

public class ScadIntVector extends ArrayList<Integer> implements IScadValue {
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(String.format("%d", this.get(i)));
        }
        result.append("]");
        return result.toString();
    }
}
