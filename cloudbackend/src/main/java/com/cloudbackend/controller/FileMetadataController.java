package com.cloudbackend.controller;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.service.FileMetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileMetadataController {

    private final FileMetadataService fileMetadataService;

    public FileMetadataController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFile(@RequestParam String name, @RequestParam String path,
                                          @RequestParam Long size, @RequestParam Long ownerId) {
        // Assuming you have a UserService to fetch user by ID
        User owner = new User(); // Fetch the user by ID in a real implementation
        owner.setId(ownerId);

        fileMetadataService.saveFileMetadata(name, path, size, owner);
        return ResponseEntity.ok("File added successfully");
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<FileMetadata>> getFilesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(fileMetadataService.getFilesByOwner(ownerId));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        fileMetadataService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully");
    }
}
