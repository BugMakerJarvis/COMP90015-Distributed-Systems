package edu.unimelb.jarvis.graph;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ShapeTypeAdapter implements JsonDeserializer<Shape>, JsonSerializer<Shape> {
    @Override
    public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get("type").getAsString();

        try {
            Class<?> clazz = Class.forName("edu.unimelb.jarvis.graph." + className);
            return context.deserialize(jsonObject, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Shape src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getClass().getName());
        jsonObject.add("data", context.serialize(src, src.getClass()));
        return jsonObject;
    }
}

