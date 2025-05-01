package project.automatedtextprocessing.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandler {
    private Map<String, TextData> entries = new HashMap<>();

    public void addData(TextData entry) {
        entries.put(entry.getId(), entry);
    }

    public TextData getData(String id) {
        return entries.get(id);
    }

    public void updateEntry(String id, String newContent) {
        TextData entry = entries.get(id);
        if (entry != null) {
            entry.setContent(newContent);
        }
    }

    public void deleteEntry(String id) {
        entries.remove(id);
    }

    public ArrayList<TextData> listAllEntries() {
        return new ArrayList<>(entries.values());
    }
}
