package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8080/users"; // Update with your backend URL

//    private static String authToken = "";

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Long> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TextField userIdField;
    @FXML
    private TextField roleField;
    @FXML
    private TextField deleteUserIdField;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("rolesAsString")); // UPDATED!

        loadUserList();
    }


    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String token = ApplicationSession.getJwtToken();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void loadUserList() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<User[]> response = restTemplate.exchange("http://localhost:8080/users/allUsers", HttpMethod.GET, entity, User[].class);

            List<User> users = Arrays.asList(response.getBody());
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            userTable.setItems(userList);
        } catch (Exception e) {
            showAlert("Error", "Failed to fetch user list.");
        }
    }

    @FXML
    private boolean updateUserRole(ActionEvent event) {
        try {
            Long userId = Long.parseLong(userIdField.getText());
            String newRole = roleField.getText().trim();

            Map<String, String> roleUpdate = new HashMap<>();
            // Create JSON request payload
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(new changeRoleRequest(userId, newRole));

            // Create HTTP request
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/role"))
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
                loadUserList();
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


    @FXML
    private void deleteUser() {
        try {
            Long userId = Long.parseLong(deleteUserIdField.getText());

            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            restTemplate.exchange(BASE_URL + "/" + userId, HttpMethod.DELETE, entity, Void.class);

            showAlert("Success", "User deleted successfully.");
            loadUserList();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete user.");
        }
    }

    @FXML
    private void homeButton() throws IOException {
            App.setRoot("File_Viewer");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    class changeRoleRequest {
        @JsonProperty
        private Long id;

        @JsonProperty
        private String role;

        public changeRoleRequest(Long userId, String role) {
            this.id = userId;
            this.role = role;
        }

    }

}
