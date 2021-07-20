package com.finance.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.interest.model.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

}
