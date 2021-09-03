package com.finance.loans.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientDAO, String> {

    Optional<ClientDAO> findById(String id);

    Optional<ClientDAO> findByPersonalCode(long personalCode);
}
