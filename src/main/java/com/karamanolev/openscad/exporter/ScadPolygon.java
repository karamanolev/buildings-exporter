package com.karamanolev.openscad.exporter;

import com.karamanolev.XY;

public class ScadPolygon extends ScadNode {
    public ScadPolygon() {
        this.type = "polygon";
    }

    public ScadPolygon(XY[][] contours) {
        this();

        ScadVector points = new ScadVector();
        ScadVector paths = new ScadVector();

        int pointIndex = 0;
        for (XY[] contour : contours) {
            ScadIntVector contourPaths = new ScadIntVector();
            for (XY point : contour) {
                points.add(ScadHelper.toDoubleVector(point));
                contourPaths.add(pointIndex++);
            }
            paths.add(contourPaths);
        }

        if (paths.size() == 1) {
            this.params = new IScadValue[]{points};
        } else {
            this.params = new IScadValue[]{points, paths};
        }
    }
}
