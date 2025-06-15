package com.simtrade.user_service.service;

import com.simtrade.common.util.JwtUtil;
import com.simtrade.common.client.MarketClient;
import com.simtrade.common.dto.UserAccountDTO;
import com.simtrade.common.dto.UserDetailsDTO;
import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.user_service.dto.*;
import com.simtrade.user_service.entity.AccountHolder;
import com.simtrade.user_service.entity.SystemState;
import com.simtrade.user_service.entity.User;
import com.simtrade.user_service.entity.User.Role;
import com.simtrade.user_service.exception.UserAlreadyExistsException;
import com.simtrade.user_service.exception.UserNotFoundException;
import com.simtrade.user_service.repository.SystemStateRepository;
import com.simtrade.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SystemStateService systemStateService;
    private final SystemStateRepository systemStateRepository;
    private final MarketClient marketClient;

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
        user.setTotalDeposits(userDTO.getInitialBalance() != null ? userDTO.getInitialBalance() : BigDecimal.ZERO);
        user.setPortfolio(new HashMap<>());
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

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return user;
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

    public BigDecimal updateBalance(String token, Long value) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
    
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    
        BigDecimal amount = BigDecimal.valueOf(value);
        BigDecimal newBalance = user.getBalance().add(amount);
        BigDecimal newTotalDeposits = user.getTotalDeposits().add(amount);
    
        user.setBalance(newBalance);
        user.setTotalDeposits(newTotalDeposits);
        userRepository.save(user);
    
        return newBalance;
    }
    
    public BigDecimal sendAmount(String token, Long recipientUserId, BigDecimal amount) {
        AccountHolder sender, recipient;

        if(token == systemStateService.getSystemState().getToken()){
            sender = systemStateService.getSystemState();
        }else{
            if (!jwtUtil.validateToken(token)) {
                throw new IllegalArgumentException("Invalid or expired token");
            }

            String senderEmail = jwtUtil.getEmailFromToken(token);
            sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() -> new UserNotFoundException("Sender not found with email: " + senderEmail));
        }
        
        if(recipientUserId == -1){
            recipient = systemStateService.getSystemState();
        }else{
            recipient = userRepository.findById(recipientUserId)
                    .orElseThrow(() -> new UserNotFoundException("Recipient not found with ID: " + recipientUserId));
        }
    
    
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
    
        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));
    
        if(sender instanceof User){
            userRepository.save((User)sender);
        }else{
            systemStateRepository.save((SystemState)sender);
        }
        if(recipient instanceof User){
            userRepository.save((User)recipient);
        }else{
            systemStateRepository.save((SystemState)recipient);
        }
    
        return sender.getBalance();
    }



    public BigDecimal calculatePortfolioValue(UserResponseDTO user) {
        BigDecimal stockValue = user.getPortfolio().entrySet().stream()
            .map(entry -> marketClient.getPrice(entry.getKey()).getPrice()
                .multiply(entry.getValue()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return user.getBalance().add(stockValue);
    }
    public UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBalance(user.getBalance());
        dto.setTotalDeposits(user.getTotalDeposits());
        dto.setPortfolio(user.getPortfolio() != null ? user.getPortfolio() : new HashMap<>());
        dto.setRole(user.getRole().name());
        dto.setInterestRate(user.getInterestRate());
        dto.setTrustable(user.getTrustable());
        dto.setEstimatedValue(calculatePortfolioValue(dto));

        return dto;
    }
    
    public UserAccountDTO mapToAccountDTO(AccountHolder user) {
        UserAccountDTO dto = new UserAccountDTO();
        dto.setSystem(!(user instanceof User));
        dto.setPortfolio(user.getPortfolio());
        dto.setBalance(user.getBalance());
        dto.setInterestRate(user.getInterestRate());
        dto.setTrustable(user.getTrustable());
        return dto;
    }

        
    public UserDetailsDTO mapToDetailsDTO(User user) {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setInterestRate(user.getInterestRate());
        dto.setTrustable(user.getTrustable());
        return dto;
    }

    public BigDecimal setInterestRate(String token, BigDecimal rate) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.setInterestRate(rate);
        userRepository.save(user);
        return rate;
    }


    public String buyAsset(String token, Long otherId, String symbol, BigDecimal price, BigDecimal quantity, boolean isBuying) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
    
        String userEmail = jwtUtil.getEmailFromToken(token);
        AccountHolder user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
    
        AccountHolder other = otherId == -1 ? systemStateService.getSystemState()
            : userRepository.findById(otherId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + otherId));
    
        AccountHolder buyer = isBuying ? user : other;
        AccountHolder seller = isBuying ? other : user;
    
        BigDecimal totalPrice = price.multiply(quantity);
    
        // 1. Check buyer balance
        if (buyer.getBalance().compareTo(totalPrice) < 0) {
            return "Buyer has insufficient balance.";
        }
    
        // 2. Check seller portfolio
        BigDecimal sellerQuantity = seller.getPortfolio().getOrDefault(symbol, BigDecimal.ZERO);
        if (sellerQuantity.compareTo(quantity) < 0) {
            return "Seller does not have enough of asset: " + symbol;
        }
    
        buyer.setBalance(buyer.getBalance().subtract(totalPrice));
        seller.setBalance(seller.getBalance().add(totalPrice));
    
        buyer.getPortfolio().put(symbol,
                buyer.getPortfolio().getOrDefault(symbol, BigDecimal.ZERO).add(quantity));
    
        seller.getPortfolio().put(symbol,
                sellerQuantity.subtract(quantity));
    
        if (buyer instanceof User userBuyer) userRepository.save(userBuyer);
        else if(buyer instanceof SystemState systemBuyer)systemStateRepository.save(systemBuyer);
        if (seller instanceof User userSeller) userRepository.save(userSeller);
        else if(buyer instanceof SystemState systemSeller)systemStateRepository.save(systemSeller);
    
        return "Trade successful";
    }
}