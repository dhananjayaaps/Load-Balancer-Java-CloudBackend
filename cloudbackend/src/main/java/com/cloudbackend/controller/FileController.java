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

import java.util.List;

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
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
        try {

            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            // Process file upload
            fileService.uploadFile(file.getOriginalFilename(), file.getBytes(), user, path ,"RR", null);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String filePath) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            byte[] fileData = fileService.downloadFile(filePath); // Pass filePath instead of fileName
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

    @GetMapping("/myfiles")
    public ResponseEntity<String> getMyFiles() {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            List<String> filePaths = fileService.getFilePathsByOwner(user.getId());
            return ResponseEntity.ok(filePaths.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving files: " + e.getMessage());
        }
    }

}
