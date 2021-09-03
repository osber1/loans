package io.osvaldas.loans.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.osvaldas.loans.repositories.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findById(String id);

    boolean existsByPersonalCode(long personalCode);
}
