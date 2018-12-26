package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScadTranslate extends ScadNode {
    private ScadTranslate() {
        this.type = "translate";
    }

    public ScadTranslate(double x, double y, double z, ScadNode... children) {
        this();
        this.params = new IScadValue[]{new ScadDoubleVector(x, y, z)};
        this.children = new ArrayList<>(Arrays.asList(children));
    }
}
