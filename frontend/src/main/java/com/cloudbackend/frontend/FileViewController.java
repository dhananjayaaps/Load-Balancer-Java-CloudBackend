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
                FileItem fileItem = findFileItemByPath(path);
                System.out.println(fileItem.canWrite());
                assert fileItem != null;

                if (fileItem != null) {
                    if (fileItem.isDirectory()) {
                        fileContentArea.clear();
                    } else if (fileItem.canRead()) {
                        loadFileContent(path);
                    } else {
                        fileContentArea.setText("Access Denied: You do not have read permission for this file.");
                        fileContentArea.setDisable(true); // Disable editing
                    }
                }
            }
        });

        saveButton.setOnAction(event -> saveFileContent());
        deleteButton.setOnAction(event -> deleteSelectedItem());
        createFileButton.setOnAction(event -> createFile());
        createDirectoryButton.setOnAction(event -> createDirectory());
    }

    private FileItem findFileItemByPath(String path) {
        for (TreeItem<String> item : rootItem.getChildren()) {
            FileItem fileItem = findFileItemRecursive(item, path);
            if (fileItem != null) {
                return fileItem;
            }
        }
        return null;
    }

    private FileItem findFileItemRecursive(TreeItem<String> item, String path) {
        if (item.getValue().equals(path)) {
            return new FileItem(path, Arrays.asList("r", "w"), item.isLeaf()); // Adjust permissions as needed
        }
        for (TreeItem<String> child : item.getChildren()) {
            FileItem found = findFileItemRecursive(child, path);
            if (found != null) {
                return found;
            }
        }
        return null;
    }


    private void loadPaths() {
        List<FileItem> fileItems = Arrays.asList(
                new FileItem("/admin/documents/Hi.txt", Arrays.asList("r", "w"), false),
                new FileItem("/admin/downloads/new/Hi.txt", Arrays.asList("r", "w"), false),
                new FileItem("/admin/downloads/new", Arrays.asList("r", "w"), true),
                new FileItem("/alex/downloads/new", Arrays.asList(), true)
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
        FileItem fileItem = findFileItemByPath(path);

        if (fileItem != null && fileItem.canWrite()) {
            try {
                Files.write(Paths.get(path), fileContentArea.getText().getBytes());
            } catch (IOException e) {
                new Alert(AlertType.ERROR, "Error saving file: " + e.getMessage()).show();
            }
        } else {
            new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file.").show();
        }
    }

    private void deleteSelectedItem() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String path = selectedItem.getValue();
            FileItem fileItem = findFileItemByPath(path);

            if (fileItem != null && fileItem.canWrite()) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                    selectedItem.getParent().getChildren().remove(selectedItem);
                } catch (IOException e) {
                    new Alert(AlertType.ERROR, "Error deleting file: " + e.getMessage()).show();
                }
            } else {
                new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file/directory.").show();
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
                FileItem parentItem = findFileItemByPath(parentPath);

                if (parentItem != null && parentItem.canWrite()) {
                    String newFilePath = parentPath + "/" + fileName;

                    try {
                        Files.createFile(Paths.get(newFilePath));
                        addFileItemToTree(new FileItem(newFilePath, Arrays.asList("r", "w"), false));
                    } catch (IOException e) {
                        new Alert(AlertType.ERROR, "Error creating file: " + e.getMessage()).show();
                    }
                } else {
                    new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this location.").show();
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
                FileItem parentItem = findFileItemByPath(parentPath);

                if (parentItem != null && parentItem.canWrite()) {
                    String newDirPath = parentPath + "/" + dirName;

                    try {
                        Files.createDirectory(Paths.get(newDirPath));
                        addFileItemToTree(new FileItem(newDirPath, Arrays.asList("r", "w"), true));
                    } catch (IOException e) {
                        new Alert(AlertType.ERROR, "Error creating directory: " + e.getMessage()).show();
                    }
                } else {
                    new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this location.").show();
                }
            }
        });
    }

}