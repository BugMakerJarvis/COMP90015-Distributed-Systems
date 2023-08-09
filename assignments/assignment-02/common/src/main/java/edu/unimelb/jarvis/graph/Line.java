package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Line extends Shape {
    private static final long serialVersionUID = -152690951352323817L;
    private double startX, startY, endX, endY;

    public Line(double startX, double startY, double endX, double endY, Color color) {
        super(color, "Line");
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.strokeLine(startX, startY, endX, endY);
    }
}
