import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller implements Initializable {

    @FXML
    private AnchorPane pane;

    @FXML
    private Label labelBoardSaved;

    @FXML
    private TextArea input;

    @FXML
    private ListView<String> toDo, inWork, done;

    private File dir = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\KanbanFX");
    private File conf = new File(this.dir + "\\kanbanFX.conf");
    private File file = new File("");

    private FileKanban fileKanban = new FileKanban();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ======================================================================
        // Application File System
        //

        if (!this.dir.exists()) {
            this.dir.mkdirs();
        }

        if (!this.conf.exists()) {
            try {
                this.conf.createNewFile();
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("KanbanFX is not working");
                alert.setContentText(
                        "Configuration File is kinda not working?!\n\nController.java line 77 has an error!");
                alert.show();
            }
        }

        // ======================================================================
        // Set input Key System
        //
        input.setOnKeyReleased(event -> {

            if (labelBoardSaved.isVisible()) {
                labelBoardSaved.setVisible(false);
            }

            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {

                if (input.getText().length() != 0) {
                    toDo.getItems().addAll(input.getText());
                    input.clear();
                }

            }
        });

        pane.setOnKeyReleased(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                saveFile();
                labelBoardSaved.setVisible(true);
            }
        });

        deleteItem(toDo);
        deleteItem(inWork);
        deleteItem(done);

        dragDetected(toDo);
        dragDone(toDo);
        dragOver(toDo);
        dragDropped(toDo);

        dragDetected(inWork);
        dragDone(inWork);
        dragOver(inWork);
        dragDropped(inWork);

        dragDetected(done);
        dragDone(done);
        dragOver(done);
        dragDropped(done);
    }

    @FXML
    private void onActionAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information about KanbanFX");
        alert.setContentText("Version: 0.1");
        alert.show();
    }

    @FXML
    private void onActionSaveBoard() {
        saveFile();
        labelBoardSaved.setVisible(true);
    }

    @FXML
    private void onActionSaveBoardAs() {
        File f = fileDialog("Save My Board");

        if (f != null) {
            this.file = new File(f.getPath());
            saveFile();
            labelBoardSaved.setVisible(true);
        }
    }

    @FXML
    private void onActionLoadBoard() {
        File f = fileDialog("Laod My Board");

        if (f != null) {
            this.file = new File(f.getPath());
            toDo.getItems().clear();
            inWork.getItems().clear();
            done.getItems().clear();
            fileKanban.loadBoard(toDo, inWork, done, f);
        }
    }

    private File fileDialog(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(this.dir);
        fileChooser.getExtensionFilters().add(new ExtensionFilter("KanbanFX", "*.kfx"));

        if (title.equals("Save My Board")) {
            return fileChooser.showSaveDialog(null);
        }

        return fileChooser.showOpenDialog(null);
    }

    private void saveFile() {

        if (this.file.getName().equals("")) {

            Stage saveStage = new Stage();
            saveStage.initModality(Modality.APPLICATION_MODAL);

            AnchorPane pane = new AnchorPane();
            pane.setStyle("-fx-background-color: #323232;");

            TextField nameField = new TextField();
            nameField.setStyle("-fx-background-color: #505050; -fx-text-fill: #fff");
            nameField.setText("MyKanban");
            nameField.setFont(new Font("Arial", 18));
            nameField.setPrefWidth(200);
            nameField.setPrefHeight(50);

            nameField.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {

                    this.file = new File(this.dir + "\\" + nameField.getText() + ".kfx");
                    fileKanban.saveBoard(toDo, inWork, done, this.file);
                    saveStage.close();

                } else if (event.getCode() == KeyCode.ESCAPE) {
                    saveStage.close();
                }
            });

            pane.getChildren().addAll(nameField);
            saveStage.setScene(new Scene(pane, 200, 50));
            saveStage.setTitle("Save Your Kanban Board");
            saveStage.setResizable(false);
            saveStage.showAndWait();
        } else {
            fileKanban.saveBoard(toDo, inWork, done, this.file);
        }
    }

    private void deleteItem(ListView<String> listView) {
        listView.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                listView.getItems().remove(listView.getSelectionModel().getSelectedIndex());
            }
        });
    }

    // ======================================================================
    // Drag und Drop Functionality
    //

    private void dragDetected(ListView<String> listView) {

        listView.setOnDragDetected(event -> {
            Dragboard db = listView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(listView.getSelectionModel().getSelectedItem());
            db.setContent(content);
            event.consume();
        });
    }

    private void dragDone(ListView<String> listView) {

        listView.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void dragOver(ListView<String> listView) {

        listView.setOnDragOver(event -> {
            if (event.getGestureSource() != listView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    private void dragDropped(ListView<String> listView) {

        listView.setOnDragDropped(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                String item = event.getDragboard().getString();
                listView.getItems().add(item);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    //
    // Drag und Drop Functionality
    // ======================================================================
}