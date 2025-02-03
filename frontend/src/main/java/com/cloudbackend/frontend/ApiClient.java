package com.cloudbackend.frontend;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/files";
    private static final RestTemplate restTemplate = new RestTemplate();

//    private static final String token = ApplicationSession.getJwtToken();
    private static final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczODYwODY3NywiZXhwIjoxNzM4NjI2Njc3fQ.UM4D8zhz7WofD9zgY1kbu8g7CJn3MqWABwjikhOeu07dtk1ARJBxl6odRuLPT0JYDj1IJb188CRELh7e6eu5QQ";

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

    // Save file content
    public static void saveFile(String path, String content) {
        HttpHeaders headers = createHeaders(token);
        HttpEntity<String> request = new HttpEntity<>(content, headers);

        restTemplate.exchange(
                BASE_URL + "/save?filePath=" + path,
                HttpMethod.POST,
                request,
                String.class);
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
}
