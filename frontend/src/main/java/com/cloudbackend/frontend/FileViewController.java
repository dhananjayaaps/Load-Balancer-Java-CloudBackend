package com.cloudbackend.frontend;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import com.cloudbackend.frontend.ApiClient;
import com.cloudbackend.frontend.FileDTO;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.ConstantBootstraps;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FileViewController {

    @FXML
    public Button initializeButton;

    @FXML
    public Button uploadFileButton;

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

    @FXML
    private Button changePermissionsButton;

    @FXML
    private Button profileButton;

    private TreeItem<String> rootItem;

    final String user = "admin";

    @FXML
    public void initialize() {
        rootItem = new TreeItem<>("Root");
        rootItem.setExpanded(true);
        fileTreeView.setRoot(rootItem);

        // Load files from backend
//        loadFilesFromBackend("/");
        loadFilesFromDB("/");
        syncFiles();

        fileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String path = newValue.getValue();
                FileDTO file = findFileByPath(path);

                if (file != null) {
                    if (file.isDirectory()) {
                        fileContentArea.clear();
                    } else if (file.isCanRead()) {
                        loadFileContentFromBackend(file.getPath());
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
        initializeButton.setOnAction(event -> initialize());
        changePermissionsButton.setOnAction(actionEvent -> showPermissionsPopup());
        uploadFileButton.setOnAction(event -> uploadFileToBackend());
        profileButton.setOnAction(actionEvent -> {
            try {
                gotoProfile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void gotoProfile() throws IOException {
        App.setRoot("ProfilePage");
    }

    private void uploadFileToBackend() {
        // Open FileChooser Dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // Convert File to Byte Array
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                // Get the selected folder in TreeView
                TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
                String uploadPath = "/"; // Default root path

                String path = getFullPathFromTreeItem(selectedItem);

                if (path.length() > 4) {
                    path = path.substring(4);
                } else {
                    path = "/";
                }

                ApiClient.uploadFile(path, file.getName(), file);

                new Alert(Alert.AlertType.INFORMATION, "File uploaded successfully!").show();

//                loadFilesFromBackend(uploadPath);
                syncFiles();
                loadFilesFromDB("/");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                new Alert(Alert.AlertType.ERROR, "Error uploading file: " + e.getMessage()).show();
            }
        }
    }


    private void loadFilesFromBackend(String path) {
        try {
            List<FileDTO> files = ApiClient.listFiles(path);
            for (FileDTO file : files) {
                addFileItemToTree(file);
                SQLiteHelper.saveFile(file);
            }
        } catch (Exception e) {
            System.out.println("Error loading files: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Error loading files: " + e.getMessage()).show();
        }
    }


    private void loadFilesFromDB(String path) {
        try {
            List<FileDTO> localFiles = SQLiteHelper.getAllFiles();
            for (FileDTO file : localFiles) {
                addFileItemToTree(file);
                SQLiteHelper.saveFile(file);
            }
        } catch (Exception e) {
            System.out.println("Error loading files: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Error loading files: " + e.getMessage()).show();
        }

    }

    private void loadFileContentFromBackend(String path) {
        try {
            byte[] fileData = ApiClient.downloadFile(path);
            if (fileData != null) {
                fileContentArea.setText(new String(fileData));
            }
            else {
                fileContentArea.setText("");
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
                ApiClient.saveFile(file.getPath(), path, fileContentArea.getText(), false, false);
                new Alert(AlertType.INFORMATION, "File saved successfully").show();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                new Alert(AlertType.ERROR, "Error saving file: " + e.getMessage()).show();
            }
        } else {
            new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file.").show();
        }
    }

    private void deleteSelectedItemFromBackend() {
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Get the full path of the selected item
            String path = getFullPathFromTreeItem(selectedItem);
            FileDTO file = findFileByPath(path);

            if (path.length() > 4) {
                path = path.substring(4);
            } else {
                path = "/";
            }

            String itemOwner = extractUsernameFromPath(path);

            if (itemOwner.startsWith("/")) {
                itemOwner = itemOwner.substring(1); // Remove the first character
            }

            if (Objects.equals(itemOwner, user)) {
                try {
                    ApiClient.deleteFileOrDirectory(path);
                    selectedItem.getParent().getChildren().remove(selectedItem);
                    syncFiles();
                    new Alert(AlertType.INFORMATION, "Deleted successfully").show();
                } catch (Exception e) {
                    new Alert(AlertType.ERROR, "Error deleting: " + e.getMessage()).show();
                }
            } else {
                new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this file/directory.").show();
            }
        }
    }

    public String extractUsernameFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length >= 1) {
            return parts[1];
        }
        return null; // No username found
    }

    private void createFileOnBackend() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create File");
        dialog.setHeaderText("Enter the file name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fileName -> {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Get the full path of the selected item
                String parentPath = getFullPathFromTreeItem(selectedItem);
                FileDTO parentFile = findFileByPath(parentPath);

                if (parentPath.length() > 4) {
                    parentPath = parentPath.substring(4);
                } else {
                    parentPath = "/";
                }

                String directoryOwner = extractUsernameFromPath(parentPath);

                if (directoryOwner.startsWith("/")) {
                    directoryOwner = directoryOwner.substring(1); // Remove the first character
                }

                if (Objects.equals(directoryOwner, user)) {
                    try {
                        String fullPath = parentPath.endsWith("/") ? parentPath + fileName : parentPath;
                        ApiClient.createFile(fullPath, fileName);
//                        loadFilesFromBackend(parentPath);
                        syncFiles();
                        loadFilesFromDB(parentPath);
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
                // Get the full path of the selected item
                String parentPath = getFullPathFromTreeItem(selectedItem);
                FileDTO parentFile = findFileByPath(parentPath);

                if (parentPath.length() > 4) {
                    parentPath = parentPath.substring(4);
                } else {
                    parentPath = "/";
                }

                String directoryOwner = extractUsernameFromPath(parentPath);

                if (directoryOwner.startsWith("/")) {
                    directoryOwner = directoryOwner.substring(1); // Remove the first character
                }

                if (Objects.equals(directoryOwner, user)) {
                    try {
                        String fullPath = parentPath.endsWith("/") ? parentPath + dirName : parentPath;
                        ApiClient.createDirectory(fullPath, dirName);
//                        loadFilesFromBackend(parentPath); // Refresh the list
                        syncFiles();
                        loadFilesFromDB(parentPath);
                    } catch (Exception e) {
                        new Alert(AlertType.ERROR, "Error creating directory: " + e.getMessage()).show();
                    }
                } else {
                    new Alert(AlertType.ERROR, "Access Denied: You do not have write permission for this location.").show();
                }
            }
        });
    }


    private FileDTO findFileByPath(String path) {
        // Traverse the tree to find the file with the given path
        return findFileByPathRecursive(rootItem, path);
    }

    private FileDTO findFileByPathRecursive(TreeItem<String> item, String path) {
        if (item.getValue().equals(path)) {
            // Check if the graphic exists before calling getUserData()
            if (item.getGraphic() != null) {
                return (FileDTO) item.getGraphic().getUserData();
            } else {
                return null; // Prevents NullPointerException
            }
        }

        for (TreeItem<String> child : item.getChildren()) {
            FileDTO found = findFileByPathRecursive(child, path);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private void addFileItemToTree(FileDTO file) {
        // Split the path into parts
        String[] parts = file.getPath().split("/");
        TreeItem<String> currentItem = rootItem;

        // Traverse the tree and create nodes as needed
        for (String part : parts) {
            if (part.isEmpty()) continue; // Skip empty parts (e.g., leading slash)

            // Check if the current part already exists in the tree
            TreeItem<String> foundItem = null;
            for (TreeItem<String> child : currentItem.getChildren()) {
                if (child.getValue().equals(part)) {
                    foundItem = child;
                    break;
                }
            }

            // If the part doesn't exist, create a new node
            if (foundItem == null) {
                foundItem = new TreeItem<>(part);
                currentItem.getChildren().add(foundItem);
            }

            // Move to the next level in the tree
            currentItem = foundItem;
        }

        // Store the FileDTO object in the TreeItem's graphic
        currentItem.setGraphic(new javafx.scene.control.Label(file.isDirectory() ? "" : "[FILE]"));
        currentItem.getGraphic().setUserData(file); // Attach the FileDTO to the TreeItem
    }

    private String getFullPathFromTreeItem(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();
        TreeItem<String> currentItem = item;

        // Traverse from the selected item up to the root
        while (currentItem != null && !currentItem.equals(rootItem)) {
            path.insert(0, currentItem.getValue()); // Prepend the current item's value
            path.insert(0, "/"); // Prepend a slash
            currentItem = currentItem.getParent(); // Move to the parent item
        }

        // If the root item is not "/", prepend its value
        if (rootItem != null && !rootItem.getValue().equals("/")) {
            path.insert(0, rootItem.getValue());
        }

        return path.toString();
    }

    private void showPermissionsPopup() {

        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            return;
        }

        // Get the path of the selected file/directory
        String path = getFullPathFromTreeItem(selectedItem);

        // Create a dialog for updating permissions
        Dialog<Pair<Boolean, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Update Permissions");
        dialog.setHeaderText("Update permissions for: " + selectedItem.getValue());

        // Set the button types (OK and Cancel)
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Create the UI components
        CheckBox canReadCheckBox = new CheckBox("Others can read");
        CheckBox canWriteCheckBox = new CheckBox("Others can write");

        // Add components to the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Read Permission:"), 0, 0);
        grid.add(canReadCheckBox, 1, 0);
        grid.add(new Label("Write Permission:"), 0, 1);
        grid.add(canWriteCheckBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // Enable/disable the update button based on input
        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(true);

        // Add listeners to enable the update button when permissions are selected
        canReadCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateButton.setDisable(false);
        });
        canWriteCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateButton.setDisable(false);
        });

        // Set the result converter to return the selected permissions
        dialog.setResultConverter(buttonType -> {
            if (buttonType == updateButtonType) {
                return new Pair<>(canReadCheckBox.isSelected(), canWriteCheckBox.isSelected());
            }
            return null;
        });

        // Show the dialog and handle the result
        Optional<Pair<Boolean, Boolean>> result = dialog.showAndWait();

        result.ifPresent(permissions -> {
            boolean canRead = permissions.getKey();
            boolean canWrite = permissions.getValue();

            try {
                String filepath = getFullPathFromTreeItem(selectedItem);
                filepath = filepath.substring(4);
                String directoryOwner = extractUsernameFromPath(filepath);

                if (directoryOwner.startsWith("/")) {
                    directoryOwner = directoryOwner.substring(1); // Remove the first character
                }

                if (directoryOwner.equals(user)){
                    ApiClient.updatePermissions(filepath, canRead, canWrite);
                }else{
                    new Alert(AlertType.ERROR, "Unauthorized").show();
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        });
    }

    private void syncFiles() {
        try {
            // Step 1: Load dataset from the local database
            List<FileDTO> localFiles = SQLiteHelper.getAllFiles();

            // Step 2: Fetch dataset from the backend endpoint (Assume the method is defined in ApiClient)
            List<FileDTO> backendFiles = ApiClient.listFiles("/");

            // Step 3: Compare the two datasets (localFiles and backendFiles)
            if (!isFileSetsEqual(localFiles, backendFiles)) {
                // Step 4: If there's a change, update the local file structure and database
                updateFileStructure(localFiles, backendFiles);
                initialize();

            } else {
                // No change, no need to do anything
                System.out.println("No changes detected between local and backend file sets.");
            }
        } catch (Exception e) {
            System.out.println("Error syncing files: " + e.getMessage());
        }
    }

    private boolean isFileSetsEqual(List<FileDTO> localFiles, List<FileDTO> backendFiles) {
        // If the number of files is different, the sets are not equal
        if (localFiles.size() != backendFiles.size()) {
            return false;
        }

        // Check if all files match based on path (and other attributes if necessary)
        for (FileDTO backendFile : backendFiles) {
            boolean fileFound = localFiles.stream().anyMatch(localFile -> localFile.getPath().equals(backendFile.getPath()));
            if (!fileFound) {
                return false;
            }
        }

        return true;
    }

    private void updateFileStructure(List<FileDTO> localFiles, List<FileDTO> backendFiles) {
        try {
            // Step 1: Identify new files that need to be added
            for (FileDTO backendFile : backendFiles) {
                FileDTO existingFile = findFileInList(localFiles, backendFile.getPath());
                if (existingFile == null) {
                    // New file, add it to the database and local structure
                    SQLiteHelper.saveFile(backendFile);
                    System.out.println("Added new file: " + backendFile.getPath());
                }
            }

            // Step 2: Identify files that need to be deleted (present in local but not in backend)
            for (FileDTO localFile : localFiles) {
                boolean existsInBackend = backendFiles.stream().anyMatch(backendFile -> backendFile.getPath().equals(localFile.getPath()));
                if (!existsInBackend) {
                    // File is deleted from backend, remove it from local database
                    SQLiteHelper.deleteFile(localFile.getPath());
                    System.out.println("Deleted file: " + localFile.getPath());
                }
            }

            // Step 3: Identify modified files (present in both local and backend but with different properties)
            for (FileDTO backendFile : backendFiles) {
                FileDTO existingFile = findFileInList(localFiles, backendFile.getPath());
                if (existingFile != null && !isFileEqual(existingFile, backendFile)) {
                    // File is modified, update it in the database
                    SQLiteHelper.saveFile(backendFile);
                    System.out.println("Updated file: " + backendFile.getPath());
                }
            }

        } catch (Exception e) {
            System.out.println("Error updating file structure: " + e.getMessage());
        }
    }

    private FileDTO findFileInList(List<FileDTO> fileList, String path) {
        return fileList.stream()
                .filter(file -> file.getPath().equals(path))
                .findFirst()
                .orElse(null);  // Returns null if file not found
    }

    private boolean isFileEqual(FileDTO file1, FileDTO file2) {
        // Compare file properties (size, permissions, etc.)
        return file1.getSize().equals(file2.getSize()) &&
                file1.isCanRead() == file2.isCanRead() &&
                file1.isCanWrite() == file2.isCanWrite() &&
                file1.isOthersCanRead() == file2.isOthersCanRead() &&
                file1.isOthersCanWrite() == file2.isOthersCanWrite() &&
                file1.isDirectory() == file2.isDirectory();
    }

}