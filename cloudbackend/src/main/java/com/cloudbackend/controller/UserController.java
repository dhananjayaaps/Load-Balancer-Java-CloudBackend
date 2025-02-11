package com.cloudbackend.controller;

import com.cloudbackend.dto.UserDTO;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import com.cloudbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users from the database...");
        List<User> users = userService.getAllUsers();
        logger.info("Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        logger.info("Registering new user: username={}, name={}", userDTO.getUsername(), userDTO.getName());
        User createdUser = userService.createUser(userDTO.getName(), userDTO.getUsername(), userDTO.getPassword(), userDTO.getRole());
        logger.info("User created successfully with ID: {}", createdUser.getId());
        return ResponseEntity.ok(createdUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        logger.info("User with ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String roleName
    ) {
        logger.info("Updating user with ID: {}", id);
        User updatedUser = userService.updateUser(id, username, password, roleName);
        logger.info("User with ID {} updated successfully", id);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/role")
    public ResponseEntity<?> updateUserRole(@RequestBody Map<String, String> request) {
        String roleName = request.get("role");
        Long id = Long.valueOf(request.get("id"));

        if (roleName == null || roleName.trim().isEmpty()) {
            logger.warn("Role update failed: Role name is required for user ID {}", id);
            return ResponseEntity.badRequest().body("Role name is required");
        }

        logger.info("Updating role for user ID: {}, new role: {}", id, roleName);
        userService.updateUserRole(id, roleName);
        logger.info("User ID {} role updated to {}", id, roleName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            UserDetails userDetails = userDetailsService.getCurrentUser();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null || currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
                logger.warn("Password change failed for user ID {}: Missing current or new password", user.getId());
                return ResponseEntity.badRequest().body("Current password and new password are required");
            }

            logger.info("Changing password for user ID: {}", user.getId());
            userService.changePassword(user.getId(), currentPassword, newPassword);
            logger.info("Password changed successfully for user ID: {}", user.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error while changing password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
