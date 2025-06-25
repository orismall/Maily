package com.example.mailyapp.models;

import android.util.Log;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MailDeserializer implements JsonDeserializer<Mail> {
    @Override
    public Mail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            Log.d("MailDeserializer", "Raw JSON received: " + json.toString());

            JsonObject obj = json.getAsJsonObject();
            Mail mail = new Mail();

            if (obj.has("mail") && obj.get("mail").isJsonObject()) {
                obj = obj.getAsJsonObject("mail");
            }

            // ID
            if (obj.has("_id") && obj.get("_id").isJsonObject()) {
                JsonObject idObj = obj.getAsJsonObject("_id");
                if (idObj.has("$oid") && !idObj.get("$oid").isJsonNull()) {
                    mail.setId(idObj.get("$oid").getAsString());
                }
            } else if (obj.has("_id") && obj.get("_id").isJsonPrimitive()) {
                mail.setId(obj.get("_id").getAsString());
            }

            mail.setSender(obj.has("sender") && !obj.get("sender").isJsonNull() ? obj.get("sender").getAsString() : "");
            mail.setSubject(obj.has("subject") && !obj.get("subject").isJsonNull() ? obj.get("subject").getAsString() : "");
            mail.setContent(obj.has("content") && !obj.get("content").isJsonNull() ? obj.get("content").getAsString() : "");
            mail.setType(obj.has("type") && !obj.get("type").isJsonNull() ? obj.get("type").getAsString() : "");

            List<String> receivers = new ArrayList<>();
            if (obj.has("receiver") && obj.get("receiver").isJsonArray()) {
                JsonArray arr = obj.getAsJsonArray("receiver");
                for (JsonElement el : arr) {
                    receivers.add(el.getAsString());
                }
            }
            mail.setReceiver(receivers);

            if (obj.has("date") && obj.get("date").isJsonObject()) {
                JsonObject dateObj = obj.getAsJsonObject("date");
                if (dateObj.has("$date") && !dateObj.get("$date").isJsonNull()) {
                    mail.setDate(dateObj.get("$date").getAsString());
                }
            } else if (obj.has("date") && obj.get("date").isJsonPrimitive()) {
                mail.setDate(obj.get("date").getAsString());
            }

            if (obj.has("labels") && obj.get("labels").isJsonArray()) {
                List<String> labels = new ArrayList<>();
                for (JsonElement e : obj.getAsJsonArray("labels")) {
                    labels.add(e.getAsString());
                }
                mail.setLabels(labels);
            }

            return mail;

        } catch (Exception e) {
            Log.e("MailDeserializer", "Failed to deserialize mail: " + e.getMessage(), e);
            return null;  // Let Gson skip this one but log the failure
        }
    }

}