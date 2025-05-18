package com.simtrade.user_service.service;

import com.simtrade.common.util.JwtUtil;
import com.simtrade.user_service.dto.*;
import com.simtrade.user_service.entity.User;
import com.simtrade.user_service.entity.User.Role;
import com.simtrade.user_service.exception.UserAlreadyExistsException;
import com.simtrade.user_service.exception.UserNotFoundException;
import com.simtrade.user_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public UserResponseDTO registerUser(UserRegisterDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + userDTO.getEmail());
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setBalance(userDTO.getInitialBalance() != null ? userDTO.getInitialBalance() : BigDecimal.ZERO);
        user.setPortfolio(new HashMap<>()); // Initialize portfolio
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    public UserResponseDTO getUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToResponseDTO(user);
    }

    public UserResponseDTO updateUser(String token, UserUpdateDTO userUpdateDTO) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (userUpdateDTO.getFirstName() != null) user.setFirstName(userUpdateDTO.getFirstName());
        if (userUpdateDTO.getLastName() != null) user.setLastName(userUpdateDTO.getLastName());
        if (userUpdateDTO.getBalance() != null) {
            if (userUpdateDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Balance cannot be negative");
            }
            user.setBalance(userUpdateDTO.getBalance());
        }
        if (userUpdateDTO.getPortfolio() != null) user.setPortfolio(userUpdateDTO.getPortfolio());

        user = userRepository.save(user);
        return mapToResponseDTO(user);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return mapToResponseDTO(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBalance(user.getBalance());
        dto.setPortfolio(user.getPortfolio() != null ? user.getPortfolio() : new HashMap<>());
        dto.setRole(user.getRole().name());
        return dto;
    }
}