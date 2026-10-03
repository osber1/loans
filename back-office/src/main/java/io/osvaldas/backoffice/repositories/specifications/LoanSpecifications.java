package io.osvaldas.backoffice.repositories.specifications;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import io.osvaldas.api.loans.Status;
import io.osvaldas.backoffice.repositories.entities.Client_;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.entities.Loan_;

public final class LoanSpecifications {

    private LoanSpecifications() {
    }

    public static Specification<Loan> clientIdIs(String clientId) {
        return (root, query, cb) -> cb.equal(root.get(Loan_.CLIENT).get(Client_.ID), clientId);
    }

    public static Specification<Loan> loanCreatedAtOrAfter(ZonedDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(Loan_.CREATED_AT), date);
    }

    public static Specification<Loan> loanStatusIn(Collection<Status> statuses) {
        return (root, query, cb) -> root.get(Loan_.STATUS).in(statuses);
    }

    public static Specification<Loan> loanIdLessThan(long id) {
        return (root, query, cb) -> cb.lessThan(root.get(Loan_.ID), id);
    }

}
