package com.cloudbackend.service;

import com.cloudbackend.FileManager.FileService;
import com.cloudbackend.entity.Role;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.RoleRepository;
import com.cloudbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public User createUser(String name, String username, String password, String roleName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("User already exists");
        }
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }

        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        User newuser = userRepository.save(user);

        fileService.createDirectory("", username, user);

        return newuser;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User updateUser(Long userId, String username, String password, String roleName) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }

        User user = existingUser.get();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}
