package com.example.mailyapp.utils;

import com.example.mailyapp.models.Mail;

import java.util.ArrayList;
import java.util.List;

public class SearchHandler {
    public static List<Mail> filter(List<Mail> allMails, String query) {
        List<Mail> filtered = new ArrayList<>();
        for (Mail m : allMails) {
            if (m.getSubject().toLowerCase().contains(query.toLowerCase()) ||
                    m.getContent().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(m);
            }
        }
        return filtered;
    }
}
