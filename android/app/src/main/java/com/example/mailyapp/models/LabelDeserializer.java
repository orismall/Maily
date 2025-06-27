package com.example.mailyapp.models;

import android.util.Log;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LabelDeserializer implements JsonDeserializer<Label> {
    @Override
    public Label deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject obj = json.getAsJsonObject();

            // Support embedded "label" key if present
            if (obj.has("label") && obj.get("label").isJsonObject()) {
                obj = obj.getAsJsonObject("label");
            }

            // Extract name early for constructor
            String name = obj.has("name") && !obj.get("name").isJsonNull()
                    ? obj.get("name").getAsString()
                    : "Unnamed";

            Label label = new Label(name);

            // Handle Mongo-style _id
            if (obj.has("_id")) {
                JsonElement idElement = obj.get("_id");
                if (idElement.isJsonObject()) {
                    JsonObject idObj = idElement.getAsJsonObject();
                    if (idObj.has("$oid") && !idObj.get("$oid").isJsonNull()) {
                        label.setId(idObj.get("$oid").getAsString());
                    }
                } else if (idElement.isJsonPrimitive()) {
                    label.setId(idElement.getAsString());
                }
            }

            // Optional: set other fields if present
            if (obj.has("name") && !obj.get("name").isJsonNull()) {
                label.setName(obj.get("name").getAsString());
            }

            if (obj.has("color") && !obj.get("color").isJsonNull()) {
                label.setColor(obj.get("color").getAsString());
            }

            if (obj.has("mailIds") && obj.get("mailIds").isJsonArray()) {
                List<String> mailIds = new java.util.ArrayList<>();
                for (JsonElement el : obj.getAsJsonArray("mailIds")) {
                    mailIds.add(el.getAsString());
                }
                label.setMailIds(mailIds);
            }

            return label;

        } catch (Exception e) {
            android.util.Log.e("LabelDeserializer", "Failed to deserialize label: " + e.getMessage(), e);
            return null;
        }
    }

}
