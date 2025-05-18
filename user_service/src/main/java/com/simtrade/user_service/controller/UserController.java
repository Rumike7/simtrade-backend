package com.simtrade.user_service.controller;

import com.simtrade.user_service.dto.*;
import com.simtrade.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return ResponseEntity.ok(userService.getUser(token));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestHeader("Authorization") String authorizationHeader, @RequestBody UserUpdateDTO userUpdateDTO) {
        String token = authorizationHeader.replace("Bearer ", "");
        UserResponseDTO updatedUser = userService.updateUser(token, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    
}