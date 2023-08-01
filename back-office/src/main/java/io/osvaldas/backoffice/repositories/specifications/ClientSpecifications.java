package io.osvaldas.backoffice.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;

import io.osvaldas.api.clients.Status;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Client_;

public final class ClientSpecifications {

    private ClientSpecifications() {
    }

    public static Specification<Client> clientStatusIs(Status status) {
        return (root, query, cb) -> cb.equal(root.get(Client_.STATUS), status);
    }

}
