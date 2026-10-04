package io.osvaldas.backoffice.repositories;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import io.osvaldas.api.loans.Status;
import io.osvaldas.backoffice.repositories.entities.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan>, RevisionRepository<Loan, Long, Long> {

    @EntityGraph(attributePaths = "loanPostpones")
    Optional<Loan> findWithPostponesById(long id);

    @EntityGraph(attributePaths = "loanPostpones")
    List<Loan> findAllWithPostponesByClientId(String clientId);

    @EntityGraph(attributePaths = "client")
    List<Loan> findAllWithClientByStatusAndCreatedAtBeforeAndIdGreaterThanOrderByIdAsc(Status status,
                                                                                         ZonedDateTime createdBefore,
                                                                                         long afterId,
                                                                                         Limit limit);

    @Lock(PESSIMISTIC_WRITE)
    Optional<Loan> findForUpdateById(long id);
}
