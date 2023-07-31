package io.osvaldas.backoffice.repositories.specifications;

import java.time.ZonedDateTime;

import javax.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;

import io.osvaldas.api.loans.Status;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Client_;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.entities.Loan_;

public interface LoanSpecifications {

    static Specification<Loan> clientIdIs(String clientId) {
        return (root, query, cb) -> {
            Join<Loan, Client> clients = root.join(Loan_.CLIENT);
            return cb.equal(clients.get(Client_.ID), clientId);
        };
    }

    static Specification<Loan> loanCreationDateIsAfter(ZonedDateTime date) {
        return (root, query, cb) -> cb.greaterThan(root.get(Loan_.CREATED_AT), date);
    }

    static Specification<Loan> loanStatusIs(Status status) {
        return (root, query, cb) -> cb.equal(root.get(Loan_.STATUS), status);
    }

}
