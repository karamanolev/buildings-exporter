package com.karamanolev.openscad.exporter;

import java.util.ArrayList;

public class ScadNode {
    protected String type;
    protected IScadValue[] params;
    protected ArrayList<ScadNode> children;
    protected String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addChild(ScadNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append("(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(params[i]);
        }
        builder.append(")");

        if (this.children != null) {
            if (this.children.size() == 1) {
                builder.append("\n");
                builder.append(indent(this.children.get(0).toString()));
            } else {
                builder.append(" {\n");
                for (ScadNode node : children) {
                    builder.append(indent(node.toString()));
                    builder.append(";\n");
                }
                builder.append("}");
            }
        }

        if (this.comment != null) {
            builder.append(" /* ");
            builder.append(this.comment);
            builder.append(" */");
        }

        return builder.toString();
    }

    private static String indent(String code) {
        StringBuilder builder = new StringBuilder();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append("  ");
            builder.append(lines[i]);
        }
        return builder.toString();
    }
}
