package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ellipse extends Shape {
    private static final long serialVersionUID = 8339912059933558764L;
    private double centerX, centerY, radiusX, radiusY;

    public Ellipse(double centerX, double centerY, double radiusX, double radiusY, Color color) {
        super(color, "Ellipse");
        this.centerX = centerX;
        this.centerY = centerY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.strokeOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
    }
}
