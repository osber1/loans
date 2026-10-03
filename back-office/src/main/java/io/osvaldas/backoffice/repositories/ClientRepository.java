package io.osvaldas.backoffice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import io.osvaldas.backoffice.repositories.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client>, RevisionRepository<Client, String, Long> {

    boolean existsByPersonalCode(String personalCode);

}
