package com.cloudbackend.FileManager;

import com.cloudbackend.dto.PermissionUpdateDTO;
import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.FilePermission;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FileSharingService {
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateUserPermissions(Long fileId, PermissionUpdateDTO dto) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<FilePermission> existing = file.getPermissions().stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst();

        if(existing.isPresent()) {
            existing.get().setCanRead(dto.isCanRead());
            existing.get().setCanWrite(dto.isCanWrite());
        } else {
            FilePermission permission = new FilePermission();
            permission.setFile(file);
            permission.setUser(user);
            permission.setCanRead(dto.isCanRead());
            permission.setCanWrite(dto.isCanWrite());
            file.getPermissions().add(permission);
        }

        fileMetadataRepository.save(file);
    }

    public String shareFile(Long fileId, Long recipientId, String permissionType) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User recipient = userRepository.findById(recipientId
        ).orElseThrow(() -> new RuntimeException("Recipient not found"));

        FilePermission permission = new FilePermission();
        permission.setFile(file);
        permission.setUser(recipient);
        permission.setCanRead(permissionType.equals("read") || permissionType.equals("write"));
        permission.setCanWrite(permissionType.equals("write"));

        file.getPermissions().add(permission);
        fileMetadataRepository.save(file);

        return "File shared with " + recipient.getUsername();
    }
}