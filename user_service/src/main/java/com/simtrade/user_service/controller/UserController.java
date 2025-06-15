package com.simtrade.user_service.controller;

import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.common.dto.BuyAssetRequestDTO;
import com.simtrade.common.dto.UserAccountDTO;
import com.simtrade.common.dto.UserDetailsDTO;
import com.simtrade.user_service.dto.*;
import com.simtrade.user_service.entity.SystemState;
import com.simtrade.user_service.entity.User;
import com.simtrade.user_service.service.LeaderboardService;
import com.simtrade.user_service.service.SystemStateService;
import com.simtrade.user_service.service.UserService;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LeaderboardService leaderboardService;
    private final SystemStateService systemStateService;

    @GetMapping("/account/{id}")
    public ResponseEntity<UserAccountDTO> getUserAccountById(@PathVariable Long id) {
        if(id == -1){
            SystemState systemState = systemStateService.getSystemState();
            return ResponseEntity.ok(userService.mapToAccountDTO(systemState));
        }
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.mapToAccountDTO(user));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<UserDetailsDTO> getUserDetailsById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.mapToDetailsDTO(user));    }


    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @GetMapping("/profile")
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

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @PostMapping("/updateBalance/{value}")
    public ResponseEntity<BigDecimal> updateBalance(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long value
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        BigDecimal balance = userService.updateBalance(token, value);
        leaderboardService.updateUserRank(userService.getUser(token), "daily");
        return ResponseEntity.ok(balance);
    }
    
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @PostMapping("/sendAmount/{recipientUserId}/{value}")
    public ResponseEntity<BigDecimal> sendAmount(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long recipientUserId,
            @PathVariable BigDecimal value
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        BigDecimal balance = userService.sendAmount(token, recipientUserId, value);
        return ResponseEntity.ok(balance);
    }

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @PostMapping("/buyAsset")
    public ResponseEntity<String> buyAsset(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody BuyAssetRequestDTO request
        ){
        String token = authorizationHeader.replace("Bearer ", "");
        return ResponseEntity.ok(userService.buyAsset(token, request.getOtherId(), request.getSymbol(), request.getPrice(), request.getQuantity(), request.isBuying()));
    } 

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN', 'MODERATOR', 'ANALYST')")
    @PutMapping("/setInterestRate/{rate}")
    public ResponseEntity<BigDecimal> setInterestRate(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable BigDecimal rate
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        BigDecimal updatedRate = userService.setInterestRate(token, rate);
        return ResponseEntity.ok(updatedRate);
    }


    
}