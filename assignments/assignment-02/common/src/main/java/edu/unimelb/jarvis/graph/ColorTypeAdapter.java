package edu.unimelb.jarvis.graph;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorTypeAdapter extends TypeAdapter<Color> {

    @Override
    public void write(JsonWriter out, Color color) throws IOException {
        out.value(color.toString());
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        return Color.valueOf(in.nextString());
    }
}
