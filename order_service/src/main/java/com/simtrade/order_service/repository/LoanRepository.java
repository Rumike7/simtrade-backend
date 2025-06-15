package com.simtrade.order_service.repository;

import com.simtrade.common.enums.LoanStatus;
import com.simtrade.order_service.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByLenderIdOrBorrowerId(Long lenderId, Long borrowerId);
}