package com.cloudbackend.service;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TerminalService {

    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public TerminalService(FileService fileService, FileMetadataRepository fileMetadataRepository) {
        this.fileService = fileService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // mv (Move or rename a file/directory)
    public String moveFileOrDirectory(String sourcePath, String destinationPath, User user) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByPath(sourcePath)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            metadata.setPath(destinationPath);
            metadata.setOwner(user);

            fileMetadataRepository.save(metadata);
            return "Moved " + sourcePath + " to " + destinationPath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to move file/directory: " + e.getMessage(), e);
        }
    }

    // cp (Copy a file/directory)
    public String copyFileOrDirectory(String sourcePath, String destinationPath, User user) {
        try {
            String savePath = destinationPath.substring(0, destinationPath.lastIndexOf("/"));
            byte[] fileData = fileService.downloadFile(sourcePath, user, true);

            fileService.uploadFile(
                    extractNameFromPath(destinationPath),
                    fileData,
                    user,
                    savePath,
                    "RR",
                    null,
                    false,
                    false
            );

            return "Copied " + sourcePath + " to " + destinationPath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy file/directory: " + e.getMessage(), e);
        }
    }

    // ls (List files/directories in a path)
    public List<String> listFiles(String path, User user) {
        try {
            return fileService.getFilePathsByOwner(user.getId()).stream()
                    .filter(filePath -> filePath.startsWith(path))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    // Generate file tree from listFiles() result
    public List<String> generateFileTree(String path, User user) {
        Set<String> filePaths = new HashSet<>(listFiles(path, user));
        FileNode root = new FileNode("admin");

        for (String filePath : filePaths) {
            addPath(root, filePath.split("/"));
        }

        List<String> result = new ArrayList<>();
        traverseTree(root, "", result);
        return result;
    }

    // mkdir (Create a directory)
    public String createDirectory(String path, String dirName, User user) {
        try {
            fileService.createDirectory(path, dirName, user);
            return "Directory created: " + path + "/" + dirName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create directory: " + e.getMessage(), e);
        }
    }

    // ps (List running processes - emulated)
    public String listProcesses() {
        return "Running processes: [java, spring-boot, terminal-emulator]";
    }

    // whoami (Get current user)
    public String whoami(User user) {
        return "Current user: " + user.getUsername();
    }

    // nano (Emulate editing a file)
    public String editFile(String path, String content, User user) {
        try {
            byte[] fileData = content.getBytes();
            fileService.updateFile(
                    extractNameFromPath(path),
                    fileData,
                    user,
                    path,
                    "RR",
                    null,
                    false,
                    false
            );
            return "File edited: " + path;
        } catch (Exception e) {
            throw new RuntimeException("Failed to edit file: " + e.getMessage(), e);
        }
    }

    // Helper method to extract file name from a path
    private String extractNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlashIndex = path.lastIndexOf('/');
        return (lastSlashIndex == -1) ? path : path.substring(lastSlashIndex + 1);
    }

    // === File Tree Helper Classes ===
    private static class FileNode {
        String name;
        Map<String, FileNode> children = new TreeMap<>();

        FileNode(String name) {
            this.name = name;
        }
    }

    private void addPath(FileNode root, String[] parts) {
        FileNode current = root;
        for (String part : parts) {
            if (!part.isEmpty()) {
                current.children.putIfAbsent(part, new FileNode(part));
                current = current.children.get(part);
            }
        }
    }

    private void traverseTree(FileNode node, String path, List<String> result) {
        if (!node.name.equals("admin")) {
            path += " - " + node.name;
            result.add(path);
        }
        for (FileNode child : node.children.values()) {
            traverseTree(child, path, result);
        }
    }
}
