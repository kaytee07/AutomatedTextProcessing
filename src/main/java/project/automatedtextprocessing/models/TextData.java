package project.automatedtextprocessing.models;

public class TextData {
    private String id;
    private String content;

    public TextData(String id, String content) {
        this.id = id;
        this.content = content;
    }


    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextData)) return false;
        TextData entry = (TextData) o;
        return id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Content: " + content;
    }
}