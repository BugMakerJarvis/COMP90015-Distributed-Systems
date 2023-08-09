package edu.unimelb.jarvis.graph;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DrawTool {

    private final GraphicsContext gc;
    private final ColorPicker colorPicker;
    private final List<Shape> shapes = new ArrayList<>();
    private Image backgroundImage;
    private Shape previewShape;

    public DrawTool(GraphicsContext gc) {
        this.gc = gc;
        this.colorPicker = new ColorPicker(Color.BLACK);
    }

    public void setShapes(List<Shape> shapes) {
        this.shapes.clear();
        this.shapes.addAll(shapes);
    }

    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
    }

    public void drawShape(Shape shape) {
        shapes.add(shape);
        previewShape = null;
        redrawAll();
    }

    public void redrawShapes() {
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    public void redrawBackgroundImage() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0);
        }
    }

    public void redrawAll() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        redrawBackgroundImage();
        redrawShapes();
    }

    public void redrawAllWithPreview() {
        redrawAll();

        if (previewShape != null) {
            previewShape.draw(gc);
        }
    }

    public void clearShapes() {
        shapes.clear();
    }

    public void clearBackgroundImage() {
        backgroundImage = null;
    }

    public void clearAll() {
        clearShapes();
        clearBackgroundImage();
        redrawAll();
    }

    public void saveCanvasAsImage(File file) {

        if (file == null) {
            return;
        }

        WritableImage writableImage = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, writableImage);

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openImageFromFile(File file) {
        if (file == null) {
            return;
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            backgroundImage = SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearShapes();
        redrawAll();
    }

    public void drawLinePreview(double startX, double startY, double endX, double endY) {
        previewShape = new Line(startX, startY, endX, endY, colorPicker.getValue().deriveColor(0, 1, 1, 0.5));
        redrawAllWithPreview();
    }

    public void drawRectanglePreview(double startX, double startY, double width, double height) {
        previewShape = new Rectangle(startX, startY, width, height, colorPicker.getValue().deriveColor(0, 1, 1, 0.5));
        redrawAllWithPreview();
    }

    public void drawCirclePreview(double centerX, double centerY, double radius) {
        previewShape = new Circle(centerX, centerY, radius, colorPicker.getValue().deriveColor(0, 1, 1, 0.5));
        redrawAllWithPreview();
    }

    public void drawEllipsePreview(double centerX, double centerY, double radiusX, double radiusY) {
        previewShape = new Ellipse(centerX, centerY, radiusX, radiusY, colorPicker.getValue().deriveColor(0, 1, 1, 0.5));
        redrawAllWithPreview();
    }

    public void drawText(double x, double y, String text) {
        gc.setFill(colorPicker.getValue());
        gc.fillText(text, x, y);
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }
}
