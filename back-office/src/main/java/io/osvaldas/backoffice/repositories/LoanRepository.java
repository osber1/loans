package io.osvaldas.backoffice.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.osvaldas.backoffice.repositories.entities.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    Optional<Loan> findById(long id);
}
