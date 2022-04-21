package io.osvaldas.backoffice.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.osvaldas.api.clients.Status;
import io.osvaldas.backoffice.repositories.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findById(String id);

    boolean existsByPersonalCode(String personalCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Client c SET c.status = ?2 WHERE c.id = ?1")
    void changeClientStatus(String id, Status status);

    int countByIdAndLoansCreatedAtAfter(String clientId, ZonedDateTime date);
}
