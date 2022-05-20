package io.osvaldas.backoffice.repositories;

import org.springframework.data.jpa.domain.Specification;

import io.osvaldas.api.clients.Status;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Client_;

public interface Specifications {

    static Specification<Client> statusIs(Status status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Client_.STATUS), status);
    }

}
