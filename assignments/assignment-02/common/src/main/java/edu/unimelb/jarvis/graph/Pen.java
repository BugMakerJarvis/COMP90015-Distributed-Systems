package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Pen extends Shape {
    private static final long serialVersionUID = 4967917684947183924L;
    private List<Double> xPoints;
    private List<Double> yPoints;

    public Pen(Color color) {
        super(color, "Pen");
        xPoints = new ArrayList<>();
        yPoints = new ArrayList<>();
    }

    public void addPoint(double x, double y) {
        xPoints.add(x);
        yPoints.add(y);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(1);

        for (int i = 1; i < xPoints.size(); i++) {
            gc.strokeLine(xPoints.get(i - 1), yPoints.get(i - 1), xPoints.get(i), yPoints.get(i));
        }
    }
}