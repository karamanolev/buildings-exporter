package com.karamanolev.openscad.exporter;

public class ScadInt implements IScadValue {
    private int value;

    public ScadInt(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }
}
