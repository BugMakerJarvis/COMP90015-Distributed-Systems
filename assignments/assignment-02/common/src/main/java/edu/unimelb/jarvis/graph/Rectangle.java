package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangle extends Shape {
    private static final long serialVersionUID = -551895402348275211L;
    private double x, y, width, height;

    public Rectangle(double x, double y, double width, double height, Color color) {
        super(color, "Rectangle");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.strokeRect(x, y, width, height);
    }
}
