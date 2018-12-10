package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ScadDoubleVector extends ArrayList<Double> implements IScadValue {
    public ScadDoubleVector(double... scadValues) {
        super(Arrays.stream(scadValues).boxed().collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(String.format("%.5f", this.get(i)));
        }
        result.append("]");
        return result.toString();
    }
}
