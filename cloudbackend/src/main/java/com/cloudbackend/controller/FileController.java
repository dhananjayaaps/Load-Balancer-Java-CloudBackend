package com.cloudbackend.controller;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.dto.*;
import com.cloudbackend.entity.User;
import com.cloudbackend.exception.PermissionDeniedException;
import com.cloudbackend.exception.ResourceNotFoundException;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(FileService fileService,  CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.fileService = fileService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String path,
            @RequestParam(defaultValue = "false") boolean othersCanRead,
            @RequestParam(defaultValue = "false") boolean othersCanWrite) throws IOException {

        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            logger.info("Uploading file: {} to path: {} with othersCanRead: {}, othersCanWrite: {} by {}",
                    file.getOriginalFilename(), path, othersCanRead, othersCanWrite, user.getUsername());

            fileService.uploadFile(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    user,
                    path,
                    "RR",
                    Collections.singletonList(1),
                    othersCanRead,
                    othersCanWrite
            );
            logger.info("File uploaded successfully: {}", file.getOriginalFilename());
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    // New permission endpoints
    @PostMapping("/update-permissions")
    public ResponseEntity<String> updatePermissions(
            @RequestBody PermssionRequest permssionRequest) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            fileService.updateOthersPermissions(permssionRequest.getPath(), permssionRequest.isCanRead(), permssionRequest.isCanWrite(), user);
            logger.info("Permissions updated by {} successfully. Path: {}, canRead: {}, canWrite: {}",
                    user.getUsername(), permssionRequest.getPath(), permssionRequest.isCanRead(), permssionRequest.isCanWrite());

            return ResponseEntity.ok("Permissions updated successfully");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (PermissionDeniedException e) {
            logger.error("Permission denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating permissions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update permissions");
        }
    }

    @PutMapping("/{fileId}/others-permissions")
    public ResponseEntity<String> updateOthersPermissions(
            @PathVariable Long fileId,
            @RequestBody OthersPermissionDTO permissionDTO) {

        try {
            fileService.updateOthersPermissions(fileId,
                    permissionDTO.isOthersCanRead(),
                    permissionDTO.isOthersCanWrite()
            );
            logger.info("Others permissions updated successfully. File ID: {}, canRead: {}, canWrite: {}",
                    fileId, permissionDTO.isOthersCanRead(), permissionDTO.isOthersCanWrite());
            return ResponseEntity.ok("Others permissions updated");
        } catch (Exception e) {
            logger.error("Error updating permissions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating permissions: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String filePath) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Downloading file: {} by {}", filePath, user.getUsername());

            byte[] fileData = fileService.downloadFile(filePath, user, false); // Pass filePath instead of fileName
            logger.info("File downloaded successfully: {}", filePath);
            return ResponseEntity.ok(fileData);
        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping("/myfiles")
    public ResponseEntity<String> getMyFiles() {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            List<String> filePaths = fileService.getFilePathsByOwner(user.getId());
            logger.info("Files retrieved successfully: {}", userDetails.getUsername());
            return ResponseEntity.ok(filePaths.toString());

        } catch (Exception e) {
            logger.error("Error retrieving files: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving files: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileDTO>> listFiles(@RequestParam String path) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Listing files in path: {}", path);
            List<FileDTO> files = fileService.listFiles(path, user);
            logger.info("Files listed successfully: {}", path);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Error listing files: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create-file")
    public ResponseEntity<String> createFile(
            @RequestParam String path,
            @RequestParam String fileName) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Creating file: {} in path: {}", fileName, path);
            fileService.createFile(path, fileName, user);
            logger.info("File created successfully: {}", fileName);
            return ResponseEntity.ok("File created successfully");
        } catch (Exception e) {
            logger.error("Error creating file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating file: " + e.getMessage());
        }
    }

    @PostMapping("/create-directory")
    public ResponseEntity<String> createDirectory(
            @RequestParam String path,
            @RequestParam String dirName) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Creating directory: {} in path: {}", dirName, path);

            fileService.createDirectory(path, dirName, user);
            logger.info("Directory created successfully: {}", dirName);
            return ResponseEntity.ok("Directory created successfully");
        } catch (Exception e) {
            logger.error("Error creating directory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating directory: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFileOrDirectory(@RequestParam String path) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Deleting path: {} by {}", path, user.getUsername() );

            fileService.deleteFileOrDirectory(path, user);
            logger.info("Deleted successfully: {}", path);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveFile(@RequestBody UpdateRequest updateRequest) {
        try {

            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            logger.info("Saving file: {} in path: {} by {}", updateRequest.getFileName(), updateRequest.getPath(), user.getUsername());
            // Convert the string content to bytes
            byte[] fileData = updateRequest.getContent().getBytes(StandardCharsets.UTF_8);

            // Call the same file upload method
            fileService.updateFile(
                    updateRequest.getFileName(),
                    fileData,
                    user,
                    updateRequest.getPath(),
                    "RR",
                    null,
                    false,
                    false
            );
            logger.info("File saved successfully: {}", updateRequest.getFileName());
            return ResponseEntity.ok("File saved successfully");
        } catch (Exception e) {
            logger.error("Error saving file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving file: " + e.getMessage());
        }
    }
}

