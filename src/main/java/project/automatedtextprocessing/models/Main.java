package project.automatedtextprocessing.models;

import project.automatedtextprocessing.Utils.RegexMatchResult;
import project.automatedtextprocessing.models.TextFileProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TextFileProcessor processor = new TextFileProcessor();
        try {
            processor.setFilePath("input.txt");
            processor.readTextFile();


            String emailPattern = "\\b[\\w.%-]+@[\\w.-]+\\.[a-zA-Z]{2,}\\b";
            ArrayList<RegexMatchResult> matches = processor.findMatchesInFile(emailPattern);
            for (RegexMatchResult match : matches) {
                System.out.println("Found: " + match.getMatchedText() + " at " + match.getStartIndex());
            }


            for (RegexMatchResult match : matches) {}
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}