package com.cloudbackend.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.cloudbackend.frontend.ApiClient;
import com.cloudbackend.frontend.FileDTO;

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

        // Load files from backend
        loadFilesFromBackend("/");

        fileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String path = newValue.getValue();
                FileDTO file = findFileByPath(path);

                if (file != null) {
                    if (file.isDirectory()) {
                        fileContentArea.clear();
                    } else if (file.isCanRead()) {
                        loadFileContentFromBackend(path);
                    } else {
                        fileContentArea.setText("Access Denied: You do not have read permission for this file.");
                        fileContentArea.setDisable(true);
                    }
                }
            }
        });

        saveButton.setOnAction(event -> saveFileContentToBackend());
        deleteButton.setOnAction(event -> deleteSelectedItemFromBackend());
        createFileButton.setOnAction(event -> createFileOnBackend());
        createDirectoryButton.setOnAction(event -> createDirectoryOnBackend());
    }

    private void loadFilesFromBackend(String path) {
        try {
            List<FileDTO> files = ApiClient.listFiles(path);
            for (FileDTO file : files) {
                addFileItemToTree(file);
            }
        } catch (Exception e) {
            new Alert(AlertType.ERROR, "Error loading files: " + e.getMessage()).show();
        }
    }

    private void loadFileContentFromBackend(String path) {
        try {
            byte[] fileData = ApiClient.downloadFile(path);
            if (fileData != null) {
                fileContentArea.setText(new String(fileData));
            }
        } catch (Exception e) {
            fileContentArea.setText("Error reading file: " + e.getMessage());
        }
    }

    private void saveFileContentToBackend() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isLeaf()) {
            return;
        }

        String path = selectedItem.getValue();
        FileDTO file = findFileByPath(path);

        if (file != null && file.isCanWrite()) {
            try {
                ApiClient.saveFile(path, fileContentArea.getText());
                new Alert(AlertType.INFORMATION, "File saved successfully").show();
            } catch (Exception e) {
                new Alert(AlertType.ERROR, "Error saving file: " + e.getMessage()).show();
            }
        } else {
            new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file.").show();
        }
    }

    private void deleteSelectedItemFromBackend() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String path = selectedItem.getValue();
            FileDTO file = findFileByPath(path);

            if (file != null && file.isCanWrite()) {
                try {
                    ApiClient.deleteFileOrDirectory(path);
                    selectedItem.getParent().getChildren().remove(selectedItem);
                    new Alert(AlertType.INFORMATION, "Deleted successfully").show();
                } catch (Exception e) {
                    new Alert(AlertType.ERROR, "Error deleting: " + e.getMessage()).show();
                }
            } else {
                new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file/directory.").show();
            }
        }
    }

    private void createFileOnBackend() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create File");
        dialog.setHeaderText("Enter the file name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fileName -> {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String parentPath = selectedItem.getValue();
                FileDTO parentFile = findFileByPath(parentPath);

                if (parentFile != null && parentFile.isCanWrite()) {
                    try {
                        ApiClient.createFile(parentPath, fileName);
                        loadFilesFromBackend(parentPath); // Refresh the list
                    } catch (Exception e) {
                        new Alert(AlertType.ERROR, "Error creating file: " + e.getMessage()).show();
                    }
                } else {
                    new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this location.").show();
                }
            }
        });
    }

    private void createDirectoryOnBackend() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Directory");
        dialog.setHeaderText("Enter the directory name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(dirName -> {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String parentPath = selectedItem.getValue();
                FileDTO parentFile = findFileByPath(parentPath);

                if (parentFile != null && parentFile.isCanWrite()) {
                    try {
                        ApiClient.createDirectory(parentPath, dirName);
                        loadFilesFromBackend(parentPath); // Refresh the list
                    } catch (Exception e) {
                        new Alert(AlertType.ERROR, "Error creating directory: " + e.getMessage()).show();
                    }
                } else {
                    new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this location.").show();
                }
            }
        });
    }

    private com.cloudbackend.frontend.FileDTO findFileByPath(String path) {
        // Implement logic to find a file by path in the tree
        return null; // Placeholder
    }

    private void addFileItemToTree(com.cloudbackend.frontend.FileDTO file) {
        // Implement logic to add a file to the tree
    }
}