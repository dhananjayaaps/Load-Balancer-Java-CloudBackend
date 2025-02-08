package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static com.cloudbackend.frontend.FileUploader.uploadFileMultipart;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/files";
    private static final RestTemplate restTemplate = new RestTemplate();

    private static final OkHttpClient client = new OkHttpClient();

//    private static final String token = ApplicationSession.getJwtToken();
    private static final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczODk4NTczOCwiZXhwIjoxNzM5MDAzNzM4fQ." +
        "eEO8yeF9_EsY2zhGOAm-yC52o2VfXxFHqDtkwMw7SkEwqvIA_sHOYAC_VjGWXT6qsc_sqXiYuupkyNy8Ccx01w";

    public ApiClient() throws IOException {
    }

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

    public static void updatePermissions(String path, boolean canRead, boolean canWrite) throws JsonProcessingException {

        System.out.println(path + " " + canRead + " " + canWrite);
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(new ApiClient.PermissionRequest(path, canRead, canWrite));

        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/update-permissions"))
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

    @Getter
    @Setter
    private static class PermissionRequest {
        @JsonProperty
        private String path;
        @JsonProperty
        private boolean canRead;
        @JsonProperty
        private  boolean canWrite;

        public PermissionRequest(String path, boolean canRead, boolean canWrite) {
            this.path = path;
            this.canRead = canRead;
            this.canWrite = canWrite;
        }
    }

    public static void uploadFile(String path, String fileName, File file) throws Exception {
        // Correct MediaType for OkHttp
        uploadFileMultipart(path,fileName,file, token);
    }


}
