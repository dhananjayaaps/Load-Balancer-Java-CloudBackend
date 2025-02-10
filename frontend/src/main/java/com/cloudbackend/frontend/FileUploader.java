package com.cloudbackend.frontend;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class FileUploader {
    private static final OkHttpClient client = new OkHttpClient();

    public static void uploadFileMultipart(String path, String fileName, File file, String token) throws Exception {
        // Correct MediaType for OkHttp
        MediaType mediaType = MediaType.get("application/octet-stream");

        // Building the multipart request body
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("path", path + "/")
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("file", file.getName(), RequestBody.create(mediaType, file))
                .build();

        // Building the request
        Request request = new Request.Builder()
                .url("http://localhost:8080/files/upload")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)  // Replace with actual auth token
                .build();

        // Executing the request
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("File uploaded successfully: " + fileName);
            } else {
                System.out.println("Failed to upload file: " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Error during request: " + e.getMessage());
        }
    }
}
