package com.cloudbackend.controller;

import com.cloudbackend.dto.UserDTO;
import com.cloudbackend.dto.UserDetailsResponse;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import com.cloudbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public UserController(UserService userService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO.getName(), userDTO.getUsername(), userDTO.getPassword(), userDTO.getRole()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestParam String username, @RequestParam String password, @RequestParam String roleName) {
        return ResponseEntity.ok(userService.updateUser(id, username, password, roleName));
    }

    @PostMapping("/role")
    public ResponseEntity<?> updateUserRole(
//            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String roleName = request.get("role");
        Long id = Long.valueOf(request.get("id"));
        if (roleName == null || roleName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name is required");
        }

        userService.updateUserRole(id, roleName);
        return ResponseEntity.ok().build(); // Return 200 OK with no body
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request
    ) {
        UserDetails userDetails = userDetailsService.getCurrentUser();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null || currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Current password and new password are required");
        }

        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            return ResponseEntity.ok().build(); // Return 200 OK with no body
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
