package project.automatedtextprocessing.models;

import project.automatedtextprocessing.Utils.RegexMatchResult;
import project.automatedtextprocessing.exceptions.ContentNotAvailableException;
import project.automatedtextprocessing.exceptions.FileProcessingException;
import project.automatedtextprocessing.exceptions.InvalidFilePathException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.*;
import java.util.stream.Collectors;

public class TextFileProcessor {
    private static final Logger logger = Logger.getLogger(TextFileProcessor.class.getName());

    private String filePath;
    private StringBuilder fileTexts;
    private TextRegexProcessor textRegexProcessor;
    private DataHandler dataHandler;
    private int ID = 0;

    public TextFileProcessor() {
        fileTexts = new StringBuilder();
        textRegexProcessor = new TextRegexProcessor();
        dataHandler = new DataHandler();
    }

    public void setFilePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            logger.warning("Attempted to set a null or empty file path.");
            throw new InvalidFilePathException("File path cannot be null or empty");
        }

        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            logger.warning("Invalid or unreadable file: " + path);
            throw new InvalidFilePathException("Invalid or unreadable file: " + path);
        }

        this.filePath = path;
        logger.info("File path set to: " + path);
    }

    public void readTextFile() {
        if (filePath == null) {
            logger.severe("File path is not set before reading.");
            throw new InvalidFilePathException("File path is not set");
        }

        fileTexts.setLength(0);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String textLine;
            while ((textLine = br.readLine()) != null) {
                fileTexts.append(textLine).append("\n");
            }
            logger.info("Successfully read content from file: " + filePath);
        } catch (IOException e) {
            logger.severe("Error reading file: " + e.getMessage());
            throw new FileProcessingException("Error reading file: " + filePath, e);
        }
    }

    public void writeToStringBuilder(String content) {
        if (content == null || content.isEmpty()) return;
        fileTexts.setLength(0);

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            fileTexts.append(line).append(System.lineSeparator());
        }

        logger.info("Content written to StringBuilder");
    }

    public StringBuilder getAllLines() {
        if (fileTexts.length() == 0) {
            logger.warning("Attempted to access empty file text content.");
            throw new ContentNotAvailableException("No content available. Ensure the file has been read and is not empty.");
        }
        return fileTexts;
    }

    public ArrayList<RegexMatchResult> wordPatternRecognizer(String regex) {
        textRegexProcessor.compileTextPattern(regex);
        return textRegexProcessor.getAllMatches(fileTexts.toString());
    }

    public ArrayList<RegexMatchResult> regexPatternRecognizer(String regex) {
        textRegexProcessor.compileRegexPattern(regex);
        return textRegexProcessor.getAllMatches(fileTexts.toString());
    }

    public String replaceInFile(String pattern, String replacement) {
        textRegexProcessor.compileRegexPattern(pattern);
        return textRegexProcessor.replaceAllMatches(getAllLines().toString(), replacement);
    }

    public String replaceAWordInFile(String pattern, String replacement) {
        return textRegexProcessor.replaceAMatch(pattern, replacement);
    }

    public void EditFileTexts(String inputText, String outputPath) {
        try {
            fileTexts = new StringBuilder();
            fileTexts.append(inputText);
            writeToFile(outputPath, inputText);
            logger.info("File text edited and written to: " + outputPath);
        } catch (IOException e) {
            logger.severe("Failed to edit and write file: " + e.getMessage());
            throw new FileProcessingException("Failed to edit and write file", e);
        }
    }

    public int getWordFrequencies(ArrayList<String> data) {
        return data.size(); // Slightly simplified since reduce wasn't really necessary
    }

    public int getNumberOfWords() {
        String text = fileTexts.toString();
        return (int) Arrays.stream(text.split("\\s+"))
                .filter(word -> !word.isBlank())
                .count();
    }

    public static List<String> summarize(List<String> lines, int topN) {
        List<String> sentences = extractSentences(lines);
        Map<String, Long> wordFrequencies = calculateWordFrequencies(lines);
        Map<String, Long> sentenceScores = scoreSentences(sentences, wordFrequencies);
        return selectTopSentences(sentenceScores, topN);
    }

    private static List<String> extractSentences(List<String> lines) {
        return lines.stream()
                .flatMap(line -> Arrays.stream(line.split("(?<=[.!?])\\s*")))
                .collect(Collectors.toList());
    }

    private static Map<String, Long> calculateWordFrequencies(List<String> lines) {
        return lines.stream()
                .flatMap(line -> Arrays.stream(line.toLowerCase().split("\\W+")))
                .filter(word -> !word.isEmpty() && isStopWord(word))
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));
    }

    private static Map<String, Long> scoreSentences(List<String> sentences, Map<String, Long> wordFrequencies) {
        Map<String, Long> scores = new HashMap<>();
        for (String sentence : sentences) {
            long score = Arrays.stream(sentence.toLowerCase().split("\\W+"))
                    .filter(wordFrequencies::containsKey)
                    .mapToLong(wordFrequencies::get)
                    .sum();
            scores.put(sentence, score);
        }
        return scores;
    }

    private static List<String> selectTopSentences(Map<String, Long> sentenceScores, int topN) {
        return sentenceScores.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "is", "at", "which", "on", "a", "an", "and", "or", "of", "in", "to", "for", "by", "with");
        return stopWords.contains(word);
    }

    public void saveEntryToDB(String content) {
        TextData newEntry = new TextData(String.valueOf(ID), content);
        dataHandler.addData(newEntry);
        logger.info("New text entry saved with ID: " + ID);
        ID++;
    }

    public ArrayList<TextData> getAllEntries() {
        return dataHandler.listAllEntries();
    }

    public void updateEntry(String id) {
        dataHandler.updateEntry(id, fileTexts.toString());
        logger.info("Updated entry with ID: " + id);
    }

    public void deleteEntry(String id) {
        dataHandler.deleteEntry(id);
        logger.info("Deleted entry with ID: " + id);
    }

    public void writeToFile(String outputPath, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            bw.write(content);
            logger.info("Successfully wrote content to file: " + outputPath);
        } catch (IOException e) {
            logger.severe("Error writing to file: " + e.getMessage());
            throw new FileProcessingException("Error writing to file: " + outputPath, e);
        }
    }
}
