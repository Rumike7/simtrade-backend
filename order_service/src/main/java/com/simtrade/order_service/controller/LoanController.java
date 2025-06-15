package com.simtrade.order_service.controller;

import com.simtrade.common.dto.LoanRequestDTO;
import com.simtrade.common.dto.LoanResponseDTO;
import com.simtrade.common.dto.RepaymentRequestDTO;
import com.simtrade.order_service.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/request")
    public ResponseEntity<LoanResponseDTO> createLoanRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody LoanRequestDTO request) {
        String token = authorizationHeader.replace("Bearer ", "");
        LoanResponseDTO loan = loanService.createLoanRequest(token, request.getAmount(), request.getLenderId(), request.getDurationDays());
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }
    
    @PostMapping("/{loanId}/accept")
    public ResponseEntity<LoanResponseDTO> acceptLoan(
        @RequestHeader("Authorization") String authorizationHeader,        
        @PathVariable Long loanId) {
            String token = authorizationHeader.replace("Bearer ", "");
            LoanResponseDTO loan = loanService.acceptLoan(loanId, token);
            return new ResponseEntity<>(loan, HttpStatus.OK);
        }
        
    @PostMapping("/{loanId}/repay")
    public ResponseEntity<LoanResponseDTO> repayLoan(
        @RequestHeader("Authorization") String authorizationHeader,   
        @PathVariable Long loanId,
        @RequestBody RepaymentRequestDTO request) {
        String token = authorizationHeader.replace("Bearer ", "");
        LoanResponseDTO loan = loanService.repayLoan(loanId, token , request.getAmount());
        return new ResponseEntity<>(loan, HttpStatus.OK);
        
    }
    @GetMapping("/available")
    public ResponseEntity<List<LoanResponseDTO>> getAvailableLoans() {
        List<LoanResponseDTO> loans = loanService.getAvailableLoans();
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<LoanResponseDTO>> getUserLoans(
        @RequestHeader("Authorization") String authorizationHeader) {
            String token = authorizationHeader.replace("Bearer ", "");
            List<LoanResponseDTO> loans = loanService.getUserLoans(token);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long loanId) {
        LoanResponseDTO loan = loanService.getLoanById(loanId);
        return new ResponseEntity<>(loan, HttpStatus.OK);
    }
}