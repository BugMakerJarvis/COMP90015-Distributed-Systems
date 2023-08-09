package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Text extends Shape {
    private static final long serialVersionUID = 4041238685765957130L;
    private double x, y;
    private String text;

    public Text(double x, double y, String text, Color color) {
        super(color, "Text");
        this.x = x;
        this.y = y;
        this.text = text;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillText(text, x, y);
    }
}
