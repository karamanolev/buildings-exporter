package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScadDifference extends ScadNode {
    private ScadDifference() {
        this.type = "difference";
        this.params = new IScadValue[0];
    }

    public ScadDifference(ScadNode... children) {
        this();
        this.children = new ArrayList<>(Arrays.asList(children));
    }
}
