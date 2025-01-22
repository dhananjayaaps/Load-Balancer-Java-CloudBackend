/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private Button signupButton;

    @FXML
    private Button loginButton;

    private final String BASE_URL = "http://localhost:8080";

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = "USER";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username and password cannot be empty.");
            return;
        }
        try {
            // Create JSON request payload
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(new SignupRequest(name, username, password));

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle response
            if (response.statusCode() == 200) {

                showAlert(Alert.AlertType.INFORMATION, "Signup Success", "Please Login to continue!");
            } else {
                // Handle error response
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Signup page: " + e.getMessage());
        }
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    static class SignupRequest {
        @JsonProperty
        private String username;
        @JsonProperty
        private String password;
        @JsonProperty
        private String name;
        @JsonProperty
        private String role;


        public SignupRequest(String name, String username, String password) {
            this.username = username;
            this.password = password;
            this.name = name;
            this.role = "USER";
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}