package com.finance.loans.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findById(String id);

    Optional<Client> findByPersonalCode(long personalCode);
}
