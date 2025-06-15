package com.simtrade.order_service.service;

import com.simtrade.order_service.entity.Loan;
import com.simtrade.common.client.UserClient;
import com.simtrade.common.dto.LoanResponseDTO;
import com.simtrade.common.dto.UserAccountDTO;
import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.common.enums.LoanStatus;
import com.simtrade.order_service.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserClient userClient;

    @Transactional
    public LoanResponseDTO createLoanRequest(String token, BigDecimal amount, Long lenderId, Integer durationDays) {
        Long borrowerId = userClient.getUser(token).getId();

        UserAccountDTO lender = userClient.getAccount(lenderId);
        BigDecimal interestRate = lender.getInterestRate();

        Loan loan = new Loan();
        loan.setLenderId(lenderId);
        loan.setBorrowerId(borrowerId);
        loan.setAmount(amount);
        loan.setInterestRate(interestRate);
        loan.setDurationDays(durationDays);
        loan.setStatus(LoanStatus.PENDING);
        loan.setCreatedAt(LocalDateTime.now());
        loan.setUpdatedAt(LocalDateTime.now());
        loan.setCurrentAmount(amount);
        loan.setLastInterestApplied(LocalDateTime.now());

        return mapToLoanResponseDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponseDTO acceptLoan(Long loanId, String token) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Loan is not available");
        }
        BigDecimal interest = loan.getAmount()
            .multiply(loan.getInterestRate())
            .divide(BigDecimal.valueOf(100));
        loan.setCurrentAmount(loan.getAmount().add(interest));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setUpdatedAt(LocalDateTime.now());
        loan.setLastInterestApplied(LocalDateTime.now());


        userClient.sendAmount(token, loan.getBorrowerId(),loan.getAmount());
        return mapToLoanResponseDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponseDTO repayLoan(Long loanId, String token, BigDecimal repaymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        Long borrowerId = userClient.getUser(token).getId();

        UserAccountDTO borrowerAccount = userClient.getAccount(borrowerId);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalArgumentException("Loan is not active");
        }
        if (repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be positive");
        }
        if (borrowerAccount.getBalance().compareTo(repaymentAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for repayment");
        }

        if (repaymentAmount.compareTo(loan.getCurrentAmount()) > 0) {
            throw new IllegalArgumentException("Repayment amount exceeds total owed: " + loan.getCurrentAmount());
        }
        BigDecimal remaining = loan.getCurrentAmount().subtract(repaymentAmount);
        loan.setCurrentAmount(remaining);

        userClient.sendAmount(token, borrowerId, repaymentAmount);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.REPAID);
        }

        loan.setUpdatedAt(LocalDateTime.now());
        return mapToLoanResponseDTO(loanRepository.save(loan));
    }
    public List<LoanResponseDTO> getAvailableLoans() {
        List<Loan> loans = loanRepository.findByStatus(LoanStatus.PENDING);
        return loans.stream()
                    .map(this::mapToLoanResponseDTO)
                    .collect(Collectors.toList());
    }

    public List<LoanResponseDTO> getUserLoans(String token) {
        Long userId = userClient.getUser(token).getId();
        List<Loan> loans = loanRepository.findByLenderIdOrBorrowerId(userId, userId);
        return loans.stream()
                    .map(this::mapToLoanResponseDTO)
                    .collect(Collectors.toList());
    }

    public LoanResponseDTO getLoanById(Long loanId) {
        return mapToLoanResponseDTO(loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found")));
    }


    public LoanResponseDTO mapToLoanResponseDTO(Loan loan) {
        if (loan == null) {
            return null;
        }
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setId(loan.getId());
        dto.setLenderId(loan.getLenderId());      
        dto.setBorrowerId(loan.getBorrowerId()); 
        dto.setAmount(loan.getAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setDurationDays(loan.getDurationDays());
        dto.setStatus(loan.getStatus());
        dto.setCreatedAt(loan.getCreatedAt());
        dto.setUpdatedAt(loan.getUpdatedAt());
        dto.setCurrentAmount(loan.getCurrentAmount());
        dto.setLastInterestApplied(loan.getLastInterestApplied());
        return dto;
    }


    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Daily
    @Transactional
    public void applyPeriodicInterest() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (Loan loan : activeLoans) {
            if (loan.getLastInterestApplied()
                    .plusDays(loan.getDurationDays())
                    .isBefore(now)) {
                BigDecimal interest = loan.getCurrentAmount()
                        .multiply(loan.getInterestRate())
                        .divide(BigDecimal.valueOf(100));
                loan.setCurrentAmount(loan.getCurrentAmount().add(interest));
                loan.setLastInterestApplied(now);
                loan.setUpdatedAt(now);
                loanRepository.save(loan);
            }
        }
    }

}