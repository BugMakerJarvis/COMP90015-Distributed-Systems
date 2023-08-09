package edu.unimelb.jarvis.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

public abstract class Shape implements Serializable {
    private static final long serialVersionUID = -9060527808861080376L;
    protected Color color;
    protected String type;

    public Shape(Color color, String type) {
        this.color = color;
        this.type = type;
    }

    public abstract void draw(GraphicsContext gc);

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
