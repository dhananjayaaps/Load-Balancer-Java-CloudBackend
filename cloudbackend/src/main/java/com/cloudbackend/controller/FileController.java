package com.cloudbackend.controller;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.FileManager.FileSharingService;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final FileSharingService fileSharingService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public FileController(FileService fileService, FileSharingService fileSharingService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.fileService = fileService;
        this.fileSharingService = fileSharingService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {

            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            // Process file upload
            fileService.uploadFile(file.getOriginalFilename(), file.getBytes(), user, "RR", null);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String fileName) {
        try {
            // Get the current authenticated user
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            // Check if the user is authorized to download the file
            // File access logic can be added here to verify the user's ownership or permissions

            byte[] fileData = fileService.downloadFile(fileName);
            return ResponseEntity.ok(fileData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/share")
    public ResponseEntity<String> shareFile(@RequestParam Long fileId,
                                            @RequestParam Long recipientId,
                                            @RequestParam String permissionType) {
        try {
            // Perform the sharing logic
            String result = fileSharingService.shareFile(fileId, recipientId, permissionType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sharing file: " + e.getMessage());
        }
    }
}
