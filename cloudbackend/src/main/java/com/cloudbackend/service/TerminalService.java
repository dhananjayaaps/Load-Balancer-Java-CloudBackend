package com.cloudbackend.service;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.dto.FileDTO;
import com.cloudbackend.entity.User;
import com.cloudbackend.exception.PermissionDeniedException;
import com.cloudbackend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TerminalService {

    private final FileService fileService;

    @Autowired
    public TerminalService(FileService fileService) {
        this.fileService = fileService;
    }

    // mv (Move or rename a file/directory)
    public String moveFileOrDirectory(String sourcePath, String destinationPath, User user) {
        try {
            // Check if source exists
            fileService.downloadFile(sourcePath, user); // Throws exception if file doesn't exist

            // Check if destination already exists
            if (fileService.listFiles(destinationPath, user).stream()
                    .anyMatch(file -> file.getPath().equals(destinationPath))) {
                throw new IllegalArgumentException("Destination already exists");
            }

            // Create a copy of the source at the destination
            fileService.createFile(destinationPath, sourcePath.substring(sourcePath.lastIndexOf("/") + 1), user);

            // Delete the source
            fileService.deleteFileOrDirectory(sourcePath, user);

            return "Moved " + sourcePath + " to " + destinationPath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to move file/directory: " + e.getMessage(), e);
        }
    }

    // cp (Copy a file/directory)
    public String copyFileOrDirectory(String sourcePath, String destinationPath, User user) {
        try {
            // Check if source exists
            byte[] fileData = fileService.downloadFile(sourcePath, user);

            // Create a copy of the source at the destination
            fileService.uploadFile(
                    sourcePath.substring(sourcePath.lastIndexOf("/") + 1),
                    fileData,
                    user,
                    destinationPath,
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

    // tree (List directory structure recursively)
    public String listDirectoryTree(String path, User user) {
        try {
            StringBuilder tree = new StringBuilder();
            listDirectoryTreeRecursive(path, user, tree, 0);
            return tree.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list directory tree: " + e.getMessage(), e);
        }
    }

    private void listDirectoryTreeRecursive(String path, User user, StringBuilder tree, int depth) {
        List<FileDTO> files = fileService.listFiles(path, user);
        for (FileDTO file : files) {
            tree.append("  ".repeat(depth)).append(file.getPath()).append("\n");
            if (file.isDirectory()) {
                listDirectoryTreeRecursive(file.getPath(), user, tree, depth + 1);
            }
        }
    }

    // nano (Emulate editing a file)
    public String editFile(String path, String content, User user) {
        try {
            byte[] fileData = content.getBytes();
            fileService.updateFile(
                    path.substring(path.lastIndexOf("/") + 1),
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
}