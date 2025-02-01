package com.cloudbackend.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FileViewController {

    @FXML
    private TreeView<String> fileTreeView;

    @FXML
    private TextArea fileContentArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button createFileButton;

    @FXML
    private Button createDirectoryButton;

    private TreeItem<String> rootItem;

    @FXML
    public void initialize() {
        rootItem = new TreeItem<>("Root");
        rootItem.setExpanded(true);
        fileTreeView.setRoot(rootItem);

        // Load predefined paths
        loadPaths();

        fileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String path = newValue.getValue();
                if (!newValue.isLeaf()) {
                    fileContentArea.clear();
                } else {
                    loadFileContent(path);
                }
            }
        });

        saveButton.setOnAction(event -> saveFileContent());
        deleteButton.setOnAction(event -> deleteSelectedItem());
        createFileButton.setOnAction(event -> createFile());
        createDirectoryButton.setOnAction(event -> createDirectory());
    }

    private void loadPaths() {
        List<FileItem> fileItems = Arrays.asList(
                new FileItem("/admin/documents/Hi.txt", Arrays.asList("r", "w"), false),
                new FileItem("/admin/downloads/new/Hi.txt", Arrays.asList("r", "w"), false),
                new FileItem("/admin/downloads/new", Arrays.asList("r", "w"), true),
                new FileItem("/alex/downloads/new", Arrays.asList("r", "w"), true)
        );

        for (FileItem item : fileItems) {
            addFileItemToTree(item);
        }
    }

    private void addFileItemToTree(FileItem fileItem) {
        String[] parts = fileItem.getPath().split("/");
        TreeItem<String> currentItem = rootItem;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            TreeItem<String> foundItem = null;
            for (TreeItem<String> child : currentItem.getChildren()) {
                if (child.getValue().equals(part)) {
                    foundItem = child;
                    break;
                }
            }

            if (foundItem == null) {
                foundItem = new TreeItem<>(part);
                currentItem.getChildren().add(foundItem);
            }

            currentItem = foundItem;
        }
    }

    private void loadFileContent(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            fileContentArea.setText(content);
        } catch (IOException e) {
            fileContentArea.setText("Error reading file: " + e.getMessage());
        }
    }

    private void saveFileContent() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isLeaf()) {
            return;
        }

        String path = selectedItem.getValue();
        try {
            Files.write(Paths.get(path), fileContentArea.getText().getBytes());
        } catch (IOException e) {
            new Alert(AlertType.ERROR, "Error saving file: " + e.getMessage()).show();
        }
    }

    private void deleteSelectedItem() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String path = selectedItem.getValue();
            try {
                Files.deleteIfExists(Paths.get(path));
                selectedItem.getParent().getChildren().remove(selectedItem);
            } catch (IOException e) {
                new Alert(AlertType.ERROR, "Error deleting file: " + e.getMessage()).show();
            }
        }
    }

    private void createFile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create File");
        dialog.setHeaderText("Enter the file name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fileName -> {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String parentPath = selectedItem.getValue();
                String newFilePath = parentPath + "/" + fileName;

                try {
                    Files.createFile(Paths.get(newFilePath));
                    addFileItemToTree(new FileItem(newFilePath, Arrays.asList("r", "w"), false));
                } catch (IOException e) {
                    new Alert(AlertType.ERROR, "Error creating file: " + e.getMessage()).show();
                }
            }
        });
    }

    private void createDirectory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Directory");
        dialog.setHeaderText("Enter the directory name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(dirName -> {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String parentPath = selectedItem.getValue();
                String newDirPath = parentPath + "/" + dirName;

                try {
                    Files.createDirectory(Paths.get(newDirPath));
                    addFileItemToTree(new FileItem(newDirPath, Arrays.asList("r", "w"), true));
                } catch (IOException e) {
                    new Alert(AlertType.ERROR, "Error creating directory: " + e.getMessage()).show();
                }
            }
        });
    }
}