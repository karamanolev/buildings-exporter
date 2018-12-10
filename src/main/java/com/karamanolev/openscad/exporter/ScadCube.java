package com.karamanolev.openscad.exporter;

public class ScadCube extends ScadNode {
    public ScadCube(double size) {
        this.type = "cube";
        this.params = new Object[]{size};
    }
}
