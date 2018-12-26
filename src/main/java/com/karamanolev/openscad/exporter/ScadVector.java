package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScadVector extends ArrayList<IScadValue> implements IScadValue {
    public ScadVector() {
    }

    public ScadVector(IScadValue[] scadValues) {
        super(Arrays.asList(scadValues));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(this.get(i));
        }
        result.append("]");
        return result.toString();
    }
}
