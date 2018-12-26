package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScadLinearExtrude extends ScadNode {
    private ScadLinearExtrude() {
        this.type = "linear_extrude";
    }

    public ScadLinearExtrude(double height, ScadNode... children) {
        this();
        this.params = new IScadValue[]{new ScadDouble(height)};
        this.children = new ArrayList<>(Arrays.asList(children));
    }
}
