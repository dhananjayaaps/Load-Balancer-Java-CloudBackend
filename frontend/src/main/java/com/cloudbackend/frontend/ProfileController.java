package com.cloudbackend.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ProfileController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    public void initialize() {
        usernameLabel.setText(ApplicationSession.getUsername());
    }

    @FXML
    private void handleChangePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Password fields cannot be empty.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        // TODO: Send password update request to backend
        boolean success = updatePasswordInBackend(newPassword);

        if (success) {
            showAlert("Success", "Password updated successfully.");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert("Error", "Failed to update password. Try again.");
        }
    }

    @FXML
    private void handleLogout() {
        // Clear session
        ApplicationSession.clear();

        try {
            // Redirect to Login Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updatePasswordInBackend(String newPassword) {
        // Simulate backend API call (replace with actual HTTP request)
        System.out.println("Password changed to: " + newPassword);
        return true; // Simulated success
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
