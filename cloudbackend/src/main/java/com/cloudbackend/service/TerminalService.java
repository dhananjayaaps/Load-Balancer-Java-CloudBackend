package com.cloudbackend.service;

import ch.qos.logback.core.joran.sanity.Pair;
import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.dto.FileDTO;
import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import java.awt.desktop.SystemEventListener;

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
            // Check if source exists
            String savepath = destinationPath.substring(0, destinationPath.lastIndexOf("/"));
            try{
                byte[] fileData = fileService.downloadFile(sourcePath, user, true);
                // Create a copy of the source at the destination
                fileService.uploadFile(
                        destinationPath.substring(sourcePath.lastIndexOf("/") + 1),
                        fileData,
                        user,
                        savepath,
                        "RR",
                        null,
                        false,
                        false
                );
            }
            catch (Exception e){
                // Create a copy of the source at the destination
                fileService.uploadFile(
                        destinationPath.substring(sourcePath.lastIndexOf("/") + 1),
                        "".getBytes(),
                        user,
                        savepath,
                        "RR",
                        null,
                        false,
                        false
                );
            }

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


    public String listDirectoryTree(String path, User user) {
        try {
            StringBuilder tree = new StringBuilder();
            Set<String> visitedPaths = new HashSet<>();
            Queue<Pair<String, Integer>> queue = new LinkedList<>();
            queue.add(new Pair<>(path, 0));
            visitedPaths.add(path);

            while (!queue.isEmpty()) {
                Pair<String, Integer> current = queue.poll();
                String currentPath = current.first;
                int depth = current.second;

                String name = extractNameFromPath(currentPath);
                tree.append("  ".repeat(depth)).append(name).append("\n");

                List<FileMetadata> files = fileService.buildTreeForTerminal(currentPath);
                if (files != null && !files.isEmpty()) {
                    for (FileMetadata file : files) {
                        String filePath = file.getPath();

                        // Only enqueue if it is a directory. If it’s a file, simply print it.
                        if (file.isDirectory()) {
                            if (!visitedPaths.contains(filePath)) {
                                queue.add(new Pair<>(filePath, depth + 1));
                                visitedPaths.add(filePath);
                            }
                        } else {
                            // Print the file as a leaf node.
                            tree.append("  ".repeat(depth + 1))
                                    .append(extractNameFromPath(filePath))
                                    .append("\n");
                        }
                    }
                }
            }
            return tree.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list directory tree: " + e.getMessage(), e);
        }
    }



    // Helper method to extract the name from a path
    private String extractNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return path;
        }
        return path.substring(lastSlashIndex + 1);
    }

    // Define a simple Pair class if not using a suitable existing one
    private static class Pair<T, U> {
        public final T first;
        public final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
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