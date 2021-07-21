package com.finance.interest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.finance.interest.model.ClientDAO;

@Repository
public interface ClientRepository extends JpaRepository<ClientDAO, String> {

    Optional<ClientDAO> findById(String id);

    Optional<ClientDAO> findByPersonalCode(long personalCode);
}
