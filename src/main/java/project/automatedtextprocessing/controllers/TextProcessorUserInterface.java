package project.automatedtextprocessing.controllers;

import project.automatedtextprocessing.exceptions.FileProcessingException;
import project.automatedtextprocessing.exceptions.InvalidFilePathException;
import project.automatedtextprocessing.models.TextData;
import project.automatedtextprocessing.models.TextFileProcessor;
import project.automatedtextprocessing.Utils.RegexMatchResult;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextProcessorUserInterface extends Application {
    private TextFileProcessor processor;
    private TextArea textArea;
    private TextField regexField;
    private Label wordCountLabel;
    private Label matchCountLabel;
    private TextField replacePatternField;
    private TextField replacementField;
    private ListView<String> dbEntriesListView;
    private TextField summarizeField;
    private FileChooser fileChooser;
    private ScrollPane textFlowScrollPane;
    private TextFlow textFlow;
    private boolean isUpdatingEntry = false;
    private String updatingEntryId = null;

    @Override
    public void start(Stage primaryStage) {
        processor = new TextFileProcessor();
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        initializeUI(primaryStage);
    }

    private void initializeUI(Stage primaryStage) {
        primaryStage.setTitle("Text Processor");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);


        textArea.textProperty().addListener((obs, oldValue, newValue) -> {
            processor.writeToStringBuilder(newValue);
            updateWordCount();
            root.setCenter(textArea);
        });

        textFlow = new TextFlow();
        textFlow.setLineSpacing(2.0);
        textFlowScrollPane = new ScrollPane(textFlow);
        textFlowScrollPane.setFitToWidth(true);
        textFlowScrollPane.setPrefHeight(textArea.getPrefHeight());

        root.setCenter(textArea);

        GridPane controlPanel = new GridPane();
        controlPanel.setHgap(10);
        controlPanel.setVgap(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER_LEFT);

        int row = 0;

        controlPanel.add(new Label("File:"), 0, row);
        Button chooseFileButton = new Button("Choose File");
        chooseFileButton.setOnAction(e -> chooseAndLoadFile(primaryStage, root));
        controlPanel.add(chooseFileButton, 1, row);
        row++;


        controlPanel.add(new Label("Regex Pattern:"), 0, row);
        regexField = new TextField();
        regexField.setPrefWidth(200);
        controlPanel.add(regexField, 1, row);
        Button findMatchesButton = new Button("Find Matches");
        findMatchesButton.setOnAction(e -> findAndHighlightMatches(root));
        controlPanel.add(findMatchesButton, 2, row);
        row++;


        controlPanel.add(new Label("Replace Pattern:"), 0, row);
        replacePatternField = new TextField();
        replacePatternField.setPrefWidth(200);
        controlPanel.add(replacePatternField, 1, row);
        row++;

        controlPanel.add(new Label("Replacement:"), 0, row);
        replacementField = new TextField();
        replacementField.setPrefWidth(200);
        controlPanel.add(replacementField, 1, row);
        Button replaceButton = new Button("Replace");
        replaceButton.setOnAction(e -> replaceText(root));
        controlPanel.add(replaceButton, 2, row);
        row++;

        controlPanel.add(new Label("Summarize (Top N Sentences):"), 0, row);
        summarizeField = new TextField();
        summarizeField.setPrefWidth(200);
        controlPanel.add(summarizeField, 1, row);
        Button summarizeButton = new Button("Summarize");
        summarizeButton.setOnAction(e -> summarizeText(root));
        controlPanel.add(summarizeButton, 2, row);
        row++;


        HBox buttonBox = new HBox(10);
        Button saveToDBButton = new Button("Save to DB");
        saveToDBButton.setOnAction(e -> saveToDB(root));
        Button saveToFileButton = new Button("Save to File");
        saveToFileButton.setOnAction(e -> saveToFile(primaryStage, root));
        Button updateEntryButton = new Button("Update Entry");
        updateEntryButton.setOnAction(e -> updateEntry());
        Button listDBButton = new Button("List DB Entries");
        listDBButton.setOnAction(e -> listDBEntries());
        Button newTextButton = new Button("New Text");
        newTextButton.setOnAction(e -> newText(root));
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearText(root));
        buttonBox.getChildren().addAll(saveToDBButton, saveToFileButton, updateEntryButton, listDBButton, newTextButton, clearButton);
        controlPanel.add(buttonBox, 0, row, 3, 1);

        root.setTop(controlPanel);

        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));

        VBox statusPanel = new VBox(5);
        wordCountLabel = new Label("Word Count: 0");
        matchCountLabel = new Label("Matches Found: 0");
        statusPanel.getChildren().addAll(wordCountLabel, matchCountLabel);
        bottomPanel.getChildren().add(statusPanel);

        dbEntriesListView = new ListView<>();
        dbEntriesListView.setPrefHeight(100);
        dbEntriesListView.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) {
//                loadDBEntry(root);
//            }
        });
        Button deleteDBEntryButton = new Button("Delete Selected Entry");
        deleteDBEntryButton.setOnAction(e -> deleteDBEntry());
        bottomPanel.getChildren().add(new Label("Database Entries:"));
        bottomPanel.getChildren().add(dbEntriesListView);
        bottomPanel.getChildren().add(deleteDBEntryButton);

        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateWordCount();
    }

    private void chooseAndLoadFile(Stage stage, BorderPane root) {
        try {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                processor.setFilePath(file.getAbsolutePath());
                processor.readTextFile();
                textArea.setText(processor.getAllLines().toString());
                updateWordCount();
                root.setCenter(textArea); // Ensure text area is shown
                isUpdatingEntry = false;
                updatingEntryId = null;
            }
        } catch (InvalidFilePathException | FileProcessingException | IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading file: " + ex.getMessage());
        }
    }

    private void findAndHighlightMatches(BorderPane root) {
        try {
            String regex = regexField.getText();
            if (!regex.isEmpty()) {
                ArrayList<RegexMatchResult> matches = processor.regexPatternRecognizer(regex);
                matchCountLabel.setText("Matches Found: " + matches.size());

                textFlow.getChildren().clear();

                String text = textArea.getText();
                int lastIndex = 0;

                for (RegexMatchResult match : matches) {
                    int start = match.getStartIndex();
                    int end = match.getEndIndex();


                    if (lastIndex < start) {
                        Text before = new Text(text.substring(lastIndex, start));
                        textFlow.getChildren().add(before);
                    }


                    Text matchText = new Text(text.substring(start, end));
                    matchText.setStyle("-fx-fill: black; -fx-background-color: yellow; -fx-background-radius: 2;");
                    textFlow.getChildren().add(matchText);

                    lastIndex = end;
                }

                if (lastIndex < text.length()) {
                    Text remaining = new Text(text.substring(lastIndex));
                    textFlow.getChildren().add(remaining);
                }

                regexField.setText("");
                root.setCenter(textFlowScrollPane);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error finding matches: " + ex.getMessage());
        }
    }

    private void replaceText(BorderPane root) {
        try {
            String pattern = replacePatternField.getText();
            String replacement = replacementField.getText();
            if (!pattern.isEmpty()) {
                String result = processor.replaceInFile(pattern, replacement);
                textArea.setText(result);
                processor.writeToStringBuilder(result);
                updateWordCount();
                root.setCenter(textArea);
            }
            replacePatternField.setText("");
            replacementField.setText("");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error replacing text: " + ex.getMessage());
        }
    }

    private void summarizeText(BorderPane root) {
        try {
            String input = summarizeField.getText();
            int topN;
            try {
                topN = Integer.parseInt(input);
                if (topN <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid positive number for summarization");
                return;
            }
            List<String> lines = Arrays.asList(textArea.getText().split("\n"));
            List<String> summary = TextFileProcessor.summarize(lines, topN);
            StringBuilder summaryText = new StringBuilder();
            for (String sentence : summary) {
                summaryText.append(sentence).append("\n");
            }
            textArea.setText(summaryText.toString());
            processor.writeToStringBuilder(summaryText.toString());
            updateWordCount();
            root.setCenter(textArea);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error summarizing text: " + ex.getMessage());
        }
    }

    private void saveToDB(BorderPane root) {
        try {
            if (isUpdatingEntry && updatingEntryId != null) {
                processor.updateEntry(updatingEntryId);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Database entry updated");
                textArea.setText("");
                processor.writeToStringBuilder("");
                isUpdatingEntry = false;
                updatingEntryId = null;
            } else {
                processor.saveEntryToDB(textArea.getText());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Content saved to database");
            }
            listDBEntries();
            updateWordCount();
            root.setCenter(textArea);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error saving to database: " + ex.getMessage());
        }
    }

    private void saveToFile(Stage stage, BorderPane root) {
        try {
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                processor.EditFileTexts(textArea.getText(), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Content saved to file");
                if (isUpdatingEntry) {
                    textArea.setText(""); // Clear text area
                    processor.writeToStringBuilder("");
                    isUpdatingEntry = false;
                    updatingEntryId = null;
                }
                updateWordCount();
                root.setCenter(textArea); // Ensure text area is shown
            }
        } catch (FileProcessingException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error saving to file: " + ex.getMessage());
        }
    }

    private void updateEntry() {
        try {
            String selected = dbEntriesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String id = selected.split(" \\| ")[0].replace("ID: ", "");
                isUpdatingEntry = true;
                updatingEntryId = id;
                showAlert(Alert.AlertType.INFORMATION, "Info", "Entry selected for update. Modify text and click 'Save to DB' or 'Save to File' to update.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select an entry to update");
            }

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error preparing entry for update: " + ex.getMessage());
        }
    }

    private void listDBEntries() {
        try {
            ArrayList<TextData> entries = processor.getAllEntries();
            dbEntriesListView.getItems().clear();
            for (TextData entry : entries) {
                dbEntriesListView.getItems().add("ID: " + entry.getId() + " | Content: " +
                        (entry.getContent().length() > 50 ? entry.getContent().substring(0, 50) + "..." : entry.getContent()));
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error listing database entries: " + ex.getMessage());
        }
    }

//    private void loadDBEntry(BorderPane root) {
//        try {
//            String selected = dbEntriesListView.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                String id = selected.split(" \\| ")[0].replace("ID: ", "");
//                ArrayList<TextData> entries = processor.getAllEntries();
//                for (TextData entry : entries) {
//                    if (entry.getId().equals(id)) {
//                        textArea.setText(entry.getContent());
//                        processor.writeToStringBuilder(entry.getContent());
//                        updateWordCount();
//                        root.setCenter(textArea);
//                        isUpdatingEntry = false;
//                        updatingEntryId = null;
//                        break;
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Error loading database entry: " + ex.getMessage());
//        }
//    }

    private void deleteDBEntry() {
        try {
            String selected = dbEntriesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String id = selected.split(" \\| ")[0].replace("ID: ", "");
                processor.deleteEntry(id);
                listDBEntries(); // Refresh DB entries list
                showAlert(Alert.AlertType.INFORMATION, "Success", "Database entry deleted");
                if (isUpdatingEntry && updatingEntryId != null && updatingEntryId.equals(id)) {
                    isUpdatingEntry = false;
                    updatingEntryId = null;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select an entry to delete");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error deleting database entry: " + ex.getMessage());
        }
    }

    private void newText(BorderPane root) {
        textArea.setText("");
        processor.writeToStringBuilder("");
        updateWordCount();
        isUpdatingEntry = false;
        updatingEntryId = null;
        root.setCenter(textArea); // Ensure text area is shown
    }

    private void clearText(BorderPane root) {
        textArea.setText("");
        processor.writeToStringBuilder("");
        updateWordCount();
        matchCountLabel.setText("Matches Found: 0");
        isUpdatingEntry = false;
        updatingEntryId = null;
        root.setCenter(textArea); // Ensure text area is shown
    }

    private void updateWordCount() {
        try {
            int count = processor.getNumberOfWords();
            wordCountLabel.setText("Word Count: " + count);
        } catch (IllegalStateException ex) {
            wordCountLabel.setText("Word Count: 0");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}