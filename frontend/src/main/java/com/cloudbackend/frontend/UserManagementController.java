package com.cloudbackend.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    private void updateUserRole(ActionEvent event) {
        try {
            Long userId = Long.parseLong(userIdField.getText());
            String newRole = roleField.getText();

            RestTemplate restTemplate = RestTemplateConfig.createRestTemplate(); // Use the new RestTemplate

            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            String url = BASE_URL + "/" + userId + "/role?roleName=" + newRole;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

            System.out.println("Response: " + response.getBody()); // Debugging: Print response

            showAlert("Success", "User role updated successfully.");
            loadUserList();
        } catch (Exception e) {
            showAlert("Error", "Failed to update user role: " + e.getMessage());
            e.printStackTrace();
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
    private void homeButton() {
        try {
            App.setRoot("UserManagement");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
