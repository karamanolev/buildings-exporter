package com.karamanolev.openscad.exporter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScadRoot extends ScadNode {
    public ScadRoot(ScadNode... children) {
        this.children = new ArrayList<>(Arrays.asList(children));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (this.comment != null) {
            builder.append("/*\n");
            builder.append(this.comment);
            builder.append("\n*/\n\n");
        }

        for (ScadNode node : this.children) {
            builder.append(node);
            builder.append(";\n");
        }
        return builder.toString();
    }
}
