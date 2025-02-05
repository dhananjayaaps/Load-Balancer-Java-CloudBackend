package com.cloudbackend.controller;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.FileManager.FileSharingService;
import com.cloudbackend.dto.*;
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

import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String path,
            @RequestParam(defaultValue = "false") boolean othersCanRead,
            @RequestParam(defaultValue = "false") boolean othersCanWrite) {

        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            fileService.uploadFile(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    user,
                    path,
                    "RR",
                    null,
                    othersCanRead,
                    othersCanWrite
            );

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    // New permission endpoints
    @PutMapping("/{fileId}/permissions")
    public ResponseEntity<String> updateUserPermissions(
            @PathVariable Long fileId,
            @RequestBody PermissionUpdateDTO permissionDTO) {

        try {
            fileSharingService.updateUserPermissions(fileId, permissionDTO);
            return ResponseEntity.ok("Permissions updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating permissions: " + e.getMessage());
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
            return ResponseEntity.ok("Others permissions updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating permissions: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String filePath) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            byte[] fileData = fileService.downloadFile(filePath, user); // Pass filePath instead of fileName
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

    @GetMapping("/list")
    public ResponseEntity<List<FileDTO>> listFiles(@RequestParam String path) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            List<FileDTO> files = fileService.listFiles(path, user);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
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

            fileService.createFile(path, fileName, user);
            return ResponseEntity.ok("File created successfully");
        } catch (Exception e) {
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

            fileService.createDirectory(path, dirName, user);
            return ResponseEntity.ok("Directory created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating directory: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFileOrDirectory(@RequestParam String path) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

            fileService.deleteFileOrDirectory(path, user);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveFile(@RequestBody UpdateRequest updateRequest) {
        try {

            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();

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

            return ResponseEntity.ok("File saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving file: " + e.getMessage());
        }
    }
}

