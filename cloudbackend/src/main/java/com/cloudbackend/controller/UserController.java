package com.cloudbackend.controller;

import com.cloudbackend.dto.UserDTO;
import com.cloudbackend.entity.User;
import com.cloudbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String roleName = request.get("role");
        if (roleName == null || roleName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name is required");
        }

        userService.updateUserRole(id, roleName);
        return ResponseEntity.ok().build(); // Return 200 OK with no body
    }

}
