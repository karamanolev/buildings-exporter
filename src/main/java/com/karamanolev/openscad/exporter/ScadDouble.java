package com.karamanolev.openscad.exporter;

public class ScadDouble implements IScadValue {
    private double value;

    public ScadDouble(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%.5f", this.value);
    }
}
