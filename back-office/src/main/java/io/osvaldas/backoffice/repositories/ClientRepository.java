package io.osvaldas.backoffice.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import io.osvaldas.api.clients.Status;
import io.osvaldas.backoffice.repositories.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client>, RevisionRepository<Client, String, Long> {

    Optional<Client> findById(String id);

    boolean existsByPersonalCode(String personalCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Client c SET c.status = :status WHERE c.id = :id")
    void changeClientStatus(String id, Status status);

}
