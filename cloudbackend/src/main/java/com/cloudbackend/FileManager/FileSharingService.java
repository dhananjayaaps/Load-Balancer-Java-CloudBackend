package com.cloudbackend.FileManager;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.FilePermission;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FilePermissionRepository;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FileSharingService {

    private final FilePermissionRepository filePermissionRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    public FileSharingService(FilePermissionRepository filePermissionRepository,
                              FileMetadataRepository fileMetadataRepository,
                              UserRepository userRepository) {
        this.filePermissionRepository = filePermissionRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.userRepository = userRepository;
    }

    /**
     * Share a file with a recipient by assigning specific permissions.
     *
     * @param fileId         The ID of the file to share.
     * @param recipientId    The ID of the recipient user.
     * @param permissionType The type of permission (e.g., READ, WRITE).
     * @return A success message if the file was shared successfully.
     */
    public String shareFile(Long fileId, Long recipientId, String permissionType) {
        // Validate permission type
        if (!isValidPermissionType(permissionType)) {
            throw new IllegalArgumentException("Invalid permission type. Allowed values: READ, WRITE.");
        }

        // Fetch file and user
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found with ID: " + recipientId));

        // Check if permission already exists
        Optional<FilePermission> existingPermission = filePermissionRepository.findByFileAndRecipient(file, recipient);
        if (existingPermission.isPresent()) {
            throw new IllegalArgumentException("File is already shared with this user.");
        }

        // Create and save permission
        FilePermission permission = new FilePermission(file, recipient, permissionType.toUpperCase());
        filePermissionRepository.save(permission);

        return "File shared successfully with " + recipient.getUsername() + " as " + permissionType.toUpperCase();
    }

    /**
     * Validate permission type.
     *
     * @param permissionType The permission type to validate.
     * @return True if valid; otherwise false.
     */
    private boolean isValidPermissionType(String permissionType) {
        return "READ".equalsIgnoreCase(permissionType) || "WRITE".equalsIgnoreCase(permissionType);
    }
}
