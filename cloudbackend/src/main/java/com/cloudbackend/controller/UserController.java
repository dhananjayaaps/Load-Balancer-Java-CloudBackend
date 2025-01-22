package com.cloudbackend.controller;

import com.cloudbackend.dto.UserDTO;
import com.cloudbackend.entity.User;
import com.cloudbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
