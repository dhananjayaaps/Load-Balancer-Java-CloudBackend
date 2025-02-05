package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/files";
    private static final RestTemplate restTemplate = new RestTemplate();

//    private static final String token = ApplicationSession.getJwtToken();
    private static final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczODcyMTkzNCwiZXhwIjoxNzM4NzM5OTM0fQ.a2d_11K6K2l97ddIDtePZvDyvjI5Sy4OIq5vZsMAUBRCyGaLyAxfcd3H_STQeQQ99lbd0MZY8LMh77DtOD0fyQ";

    // Helper method to create headers with Bearer Token
    private static HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // Sets "Authorization: Bearer <token>"
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Fetch files from the backend
    public static List<FileDTO> listFiles(String path) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<FileDTO[]> response = restTemplate.exchange(
                BASE_URL + "/list?path=" + path,
                HttpMethod.GET,
                entity,
                FileDTO[].class);

        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
    }

    // Download file content
    public static byte[] downloadFile(String path) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                BASE_URL + "/download?filePath=" + path,
                HttpMethod.GET,
                entity,
                byte[].class);

        return response.getBody();
    }

    public static void saveFile(String path, String filename, String content, boolean othersCanRead, boolean othersCanWrite) {
        HttpHeaders headers = createHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);  // Ensure JSON format

        System.out.println("path: " + path);
        System.out.println("filename: " + filename);
        System.out.println("content: " + content);

        try {
            // Create JSON request payload
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(new ApiClient.UpdateRequest(path, filename, content));

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/save"))
                    .header("Authorization", "Bearer " + token) // Correct way to add Bearer token
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the server: " + e.getMessage());
        }
    }

    // Delete file or directory
    public static void deleteFileOrDirectory(String path) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                BASE_URL + "/delete?path=" + path,
                HttpMethod.DELETE,
                entity,
                String.class);
    }

    // Create a file
    public static void createFile(String path, String fileName) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                BASE_URL + "/create-file?path=" + path + "&fileName=" + fileName,
                HttpMethod.POST,
                entity,
                String.class);
    }

    // Create a directory
    public static void createDirectory(String path, String dirName) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                BASE_URL + "/create-directory?path=" + path + "&dirName=" + dirName,
                HttpMethod.POST,
                entity,
                String.class);
    }

    @Getter
    @Setter
    static
    class UpdateRequest {
        @JsonProperty
        private String path;
        @JsonProperty
        private String filename;
        @JsonProperty
        private String content;

        public UpdateRequest(String path, String fileName, String content) {
            this.filename = fileName;
            this.content = content;
            this.path = path;
        }

    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
