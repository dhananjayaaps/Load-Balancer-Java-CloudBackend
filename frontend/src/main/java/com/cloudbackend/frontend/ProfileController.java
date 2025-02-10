package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.http.*;
import com.cloudbackend.frontend.RestTemplateConfig.*;
import org.springframework.web.client.RestTemplate;

import java.io.Console;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.cloudbackend.frontend.RestTemplateConfig.createRestTemplate;

public class ProfileController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    public void initialize() {
        usernameLabel.setText(ApplicationSession.getUsername());
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "All password fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New passwords do not match.");
            return;
        }

        boolean success = updatePasswordInBackend(currentPassword, newPassword);

        if (success) {
            showAlert("Success", "Password updated successfully.");
            currentPasswordField.clear();
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

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String token = ApplicationSession.getJwtToken();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

//    private boolean updatePasswordInBackend(String currentPassword, String newPassword) {
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            System.out.println("Updating password..." + currentPassword + " " + newPassword);
//
//            // Create the PATCH request
//            HttpPatch httpPatch = new HttpPatch("http://localhost:8080/users/change-password");
//
//            // Set headers
//            httpPatch.setHeader("Content-Type", "application/json");
//
//            // Create the request body
//            String jsonBody = String.format(
//                    "{\"currentPassword\": \"%s\", \"newPassword\": \"%s\"}",
//                    currentPassword, newPassword
//            );
//            HttpEntity entity = new HttpEntity(jsonBody);
//            httpPatch.setEntity((org.apache.hc.core5.http.HttpEntity) entity);
//
//            // Execute the request
//            try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {
//                int statusCode = response.getCode();
//                if (statusCode == 200) {
//                    showAlert("Success", "Password updated successfully.");
//                    return true;
//                } else {
//                    String responseBody = EntityUtils.toString(response.getEntity());
//                    showAlert("Error", "Update failed: " + responseBody);
//                    return false;
//                }
//            }
//        } catch (Exception e) {
//            showAlert("Error", "Update failed: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }

    @FXML
    private boolean updatePasswordInBackend(String currentPassword, String newPassword) {

        try {
            // Create JSON request payload
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(new ChangePasswordRequest(currentPassword, newPassword));

            // Create HTTP request
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/users/change-password"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ApplicationSession.getJwtToken())
                    .method("POST", java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            // Handle response
            if (response.statusCode() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
//            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void homeButton() throws IOException {
        App.setRoot("File_Viewer");
    }

    class ChangePasswordRequest {
        @JsonProperty
        private String currentPassword;
        @JsonProperty
        private String newPassword;

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

    }
}