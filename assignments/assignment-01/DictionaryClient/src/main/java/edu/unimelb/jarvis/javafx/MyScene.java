package edu.unimelb.jarvis.javafx;

import edu.unimelb.jarvis.client.TCPInteractiveClient;
import edu.unimelb.jarvis.dict.Word;
import io.vproxy.vfx.control.scroll.VScrollPane;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.layout.VPadding;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MyScene extends VScene {
    public MyScene() {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();

        VTableView<Word> table = new VTableView<>();
        table.getNode().setMinWidth(1196);
        table.getNode().setPrefHeight(672);

        VTableColumn<Word, Long> idCol = new VTableColumn<>("ID", Word::getId);
        VTableColumn<Word, String> wordCol = new VTableColumn<>("WORD", Word::getWord);
        VTableColumn<Word, String> meaningCol = new VTableColumn<>("MEANING", Word::getMeaning);

        idCol.setMaxWidth(128);
        idCol.setAlignment(Pos.CENTER);
        wordCol.setMaxWidth(256);
        wordCol.setComparator(String::compareTo);
        wordCol.setAlignment(Pos.CENTER);

        // noinspection unchecked
        table.getColumns().addAll(idCol, wordCol, meaningCol);

        // get the instance of TCPInteractiveClient
        TCPInteractiveClient client = TCPInteractiveClient.getInstance();

        List<Word> dictionary = client.receiveDictionaryFromServer();
        if (dictionary == null) {
            dictionary = new ArrayList<>();
            log.info("No dictionary received from the server");
        }
        table.setItems(dictionary);

        FusionButton addButton = new FusionButton("Add") {{
            setOnAction(e -> {
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Add a new word");

                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(submitButtonType);

                Label wordLabel = new Label("Word:");
                TextField wordField = new TextField();
                Platform.runLater(wordField::requestFocus);
                Label meaningLabel = new Label("Meaning:");
                TextArea meaningArea = new TextArea();
                meaningArea.setWrapText(true);

                // disable submit button if word or meaning is empty
                Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
                ChangeListener<String> textFieldsListener = (observable, oldValue, newValue) -> {
                    boolean disableButton = wordField.getText().trim().isEmpty() || meaningArea.getText().trim().isEmpty();
                    submitButton.setDisable(disableButton);
                };
                wordField.textProperty().addListener(textFieldsListener);
                meaningArea.textProperty().addListener(textFieldsListener);
                submitButton.setDisable(true);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                grid.add(wordLabel, 1, 1);
                grid.add(wordField, 2, 1);
                grid.add(meaningLabel, 1, 2);
                grid.add(meaningArea, 2, 2);
                dialog.getDialogPane().setContent(grid);

                grid.setStyle(String.format("-fx-background-color: %s; -fx-font-size: %s;", MyTheme.COLOR_BACKGROUND, MyTheme.SIZE_TEXT));
                wordLabel.setStyle(String.format("-fx-text-fill: %s;", MyTheme.COLOR_TEXT));
                wordField.setStyle(String.format("-fx-text-fill: %s; -fx-background-color: %s; -fx-border-color: %s; -fx-border-style: solid; -fx-border-width: 1px; -fx-background-radius: 2; -fx-border-radius: 2;", MyTheme.COLOR_TEXT, MyTheme.COLOR_BACKGROUND, MyTheme.COLOR_BORDER));
                meaningLabel.setStyle(String.format("-fx-text-fill: %s;", MyTheme.COLOR_TEXT));
                meaningArea.setStyle(String.format("-fx-text-fill: %s; -fx-control-inner-background: %s;-fx-pref-height: 100;", MyTheme.COLOR_TEXT, MyTheme.COLOR_BACKGROUND));
                dialog.getDialogPane().setStyle(String.format("-fx-background-color: %s;", MyTheme.COLOR_BACKGROUND));

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        return wordField.getText() + " " + meaningArea.getText().trim().replaceAll("\n", "");
                    }
                    return null;
                });
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String res = client.sendMessageToServer("ADD " + result.get());
                    String status = res.split(" ")[0];
                    List<Word> dictionary = client.receiveDictionaryFromServer();
                    if (dictionary == null) {
                        dictionary = new ArrayList<>();
                        log.info("No dictionary received from the server");
                    }
                    if (!status.equals("SUCCESS")) SimpleAlert.showAndWait(Alert.AlertType.INFORMATION, res);
                    table.setItems(dictionary);
                } else {
                    log.info("No word added");
                }
            });
            setPrefWidth(128);
            setPrefHeight(32);
        }};

        FusionButton deleteButton = new FusionButton("Delete") {{
            setOnAction(e -> {
                Word selected = table.getSelectedItem();
                if (selected == null) {
                    return;
                }
                String res = client.sendMessageToServer("DELETE " + selected.getId());
                String status = res.split(" ")[0];
                List<Word> dictionary = client.receiveDictionaryFromServer();
                if (dictionary == null) {
                    dictionary = new ArrayList<>();
                    log.info("No dictionary received from the server");
                }
                if (!status.equals("SUCCESS")) SimpleAlert.showAndWait(Alert.AlertType.INFORMATION, res);
                table.setItems(dictionary);
            });
            setPrefWidth(128);
            setPrefHeight(32);
        }};

        FusionButton updateButton = new FusionButton("Update") {{
            setOnAction(e -> {
                Word selected = table.getSelectedItem();
                if (selected == null) {
                    return;
                }
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Update the word");

                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(submitButtonType);

                Label wordLabel = new Label("Word:");
                TextField wordField = new TextField(selected.getWord());
                wordField.setEditable(false);
                Label meaningLabel = new Label("Meaning:");
                TextArea meaningArea = new TextArea(selected.getMeaning());
                Platform.runLater(meaningArea::requestFocus);
                meaningArea.setWrapText(true);

                // disable submit button if word or meaning is empty
                Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
                ChangeListener<String> textFieldsListener = (observable, oldValue, newValue) -> {
                    boolean disableButton = wordField.getText().trim().isEmpty() || meaningArea.getText().trim().isEmpty();
                    submitButton.setDisable(disableButton);
                };
                wordField.textProperty().addListener(textFieldsListener);
                meaningArea.textProperty().addListener(textFieldsListener);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                grid.add(wordLabel, 1, 1);
                grid.add(wordField, 2, 1);
                grid.add(meaningLabel, 1, 2);
                grid.add(meaningArea, 2, 2);
                dialog.getDialogPane().setContent(grid);

                grid.setStyle(String.format("-fx-background-color: %s; -fx-font-size: %s;", MyTheme.COLOR_BACKGROUND, MyTheme.SIZE_TEXT));
                wordLabel.setStyle(String.format("-fx-text-fill: %s;", MyTheme.COLOR_TEXT));
                wordField.setStyle(String.format("-fx-text-fill: %s; -fx-background-color: %s; -fx-border-color: %s; -fx-border-style: solid; -fx-border-width: 1px; -fx-background-radius: 2; -fx-border-radius: 2;", MyTheme.COLOR_TEXT, MyTheme.COLOR_BACKGROUND, MyTheme.COLOR_BORDER));
                meaningLabel.setStyle(String.format("-fx-text-fill: %s;", MyTheme.COLOR_TEXT));
                meaningArea.setStyle(String.format("-fx-text-fill: %s; -fx-control-inner-background: %s;-fx-pref-height: 100;", MyTheme.COLOR_TEXT, MyTheme.COLOR_BACKGROUND));
                dialog.getDialogPane().setStyle(String.format("-fx-background-color: %s;", MyTheme.COLOR_BACKGROUND));

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        return meaningArea.getText().trim().replaceAll("\n", "");
                    }
                    return null;
                });
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String res = client.sendMessageToServer("UPDATE " + selected.getId() + " " + selected.getWord() + " " + result.get());
                    String status = res.split(" ")[0];
                    List<Word> dictionary = client.receiveDictionaryFromServer();
                    if (dictionary == null) {
                        dictionary = new ArrayList<>();
                        log.info("No dictionary received from the server");
                    }
                    if (!status.equals("SUCCESS")) SimpleAlert.showAndWait(Alert.AlertType.INFORMATION, res);
                    table.setItems(dictionary);
                } else {
                    log.info("No word updated");
                }
            });
            setPrefWidth(128);
            setPrefHeight(32);
        }};

        FusionButton reloadButton = new FusionButton("Reload") {{
            setOnAction(e -> {
                if (!client.isAlive()) {
                    Thread t = new Thread(client);
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        log.error("Error waiting for TCPInteractiveClient thread to complete: " + ex.getMessage());
                    }
                }

                List<Word> dictionary = client.receiveDictionaryFromServer();
                if (dictionary == null) {
                    dictionary = new ArrayList<>();
                    log.info("No dictionary received from the server");
                }
                table.setItems(dictionary);
            });
            setPrefWidth(128);
            setPrefHeight(32);
        }};

        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(128);
        searchField.setPrefHeight(32);
        searchField.setStyle(String.format("-fx-text-fill: %s; -fx-background-color: %s; -fx-border-color: %s; -fx-border-style: solid; -fx-border-width: 1px; -fx-background-radius: 2; -fx-border-radius: 2;", MyTheme.COLOR_TEXT, MyTheme.COLOR_BACKGROUND, MyTheme.COLOR_BORDER));

        FusionButton searchButton = new FusionButton("Search") {{
            setOnAction(e -> {
                String word = searchField.getText();
                if (word.equals("")) {
                    List<Word> dictionary = client.receiveDictionaryFromServer();
                    if (dictionary == null) {
                        log.info("No words received from the server");
                    } else {
                        table.setItems(dictionary);
                    }
                } else {
                    List<Word> words = client.receiveWordsFromServer(word);
                    if (words == null) {
                        log.info("No words received from the server");
                    } else {
                        table.setItems(words);
                    }
                }
            });
            setPrefWidth(96);
            setPrefHeight(32);
            setDisableAnimation(true);
            setBorder(new Border(new BorderStroke(Color.web(MyTheme.COLOR_BORDER), BorderStrokeStyle.DASHED, new CornerRadii(2), new BorderWidths(1))));
        }};

        // Layout
        GridPane gridPane = new GridPane();
        HBox leftBox = new HBox(16, addButton, deleteButton, updateButton, reloadButton);
        GridPane.setHgrow(leftBox, Priority.ALWAYS);
        HBox searchBox = new HBox(16, searchField, searchButton);
        gridPane.addRow(0, leftBox, searchBox);

        VScrollPane hScrollPane = VScrollPane.makeHorizontalScrollPaneToManage(table);
        hScrollPane.getNode().setPrefWidth(1196);

        VBox vbox = new VBox(new VPadding(16), gridPane, new VPadding(16), hScrollPane.getNode());
        FXUtils.observeWidthCenter(getContentPane(), vbox);

        getContentPane().getChildren().addAll(vbox);
    }
}
