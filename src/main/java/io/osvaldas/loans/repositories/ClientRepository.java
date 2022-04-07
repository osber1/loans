package io.osvaldas.loans.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.osvaldas.loans.repositories.entities.Client;
import io.osvaldas.loans.repositories.entities.Status;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findById(String id);

    boolean existsByPersonalCode(long personalCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Client c SET c.status = ?2 WHERE c.id = ?1")
    void changeClientStatus(String id, Status status);
}
