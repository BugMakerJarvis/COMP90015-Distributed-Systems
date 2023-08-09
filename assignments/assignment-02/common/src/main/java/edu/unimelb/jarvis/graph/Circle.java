package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circle extends Shape {
    private static final long serialVersionUID = 7428142105888327107L;
    private double centerX, centerY, radius;

    public Circle(double centerX, double centerY, double radius, Color color) {
        super(color, "Circle");
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
}
