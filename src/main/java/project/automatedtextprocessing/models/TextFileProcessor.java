package project.automatedtextprocessing.models;

import project.automatedtextprocessing.Utils.RegexMatchResult;
import java.io.*;
import java.util.ArrayList;

public class TextFileProcessor {
    private String filePath;
    private StringBuilder fileTexts;
    private TextRegexProcessor textRegexProcessor;

    public TextFileProcessor() {
        fileTexts = new StringBuilder();
        textRegexProcessor = new TextRegexProcessor();
    }

    public void setFilePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Invalid or unreadable file: " + path);
        }
        filePath = path;
    }

    public void readTextFile() throws IOException {
        if (filePath == null) {
            throw new IllegalStateException("File path is not set");
        }
        fileTexts.setLength(0);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String textLine;
            while ((textLine = br.readLine()) != null) {
                fileTexts.append(textLine).append("\n");
            }
        }
    }

    public String getAllLines() {
        if (fileTexts.length() == 0) {
            throw new IllegalStateException("No content available. Ensure the file has been read and is not empty.");
        }
        return fileTexts.toString();
    }

    public ArrayList<RegexMatchResult> findMatchesInFile(String pattern) {
        textRegexProcessor.compileRegexPattern(pattern);
        return textRegexProcessor.getAllMatches(getAllLines());
    }

    public String replaceInFile(String pattern, String replacement) {
        textRegexProcessor.compileRegexPattern(pattern);
        return textRegexProcessor.replaceAllMatches(getAllLines(), replacement);
    }

    public void writeToFile(String outputPath, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            bw.write(content);
        }
    }
}
