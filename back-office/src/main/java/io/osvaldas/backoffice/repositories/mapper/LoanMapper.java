package io.osvaldas.backoffice.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.api.loans.LoanRequest;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.repositories.entities.Loan;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    Collection<LoanResponse> loanToDTOs(Collection<Loan> all);

    Loan loanToEntity(LoanRequest request);

    LoanResponse loanToDTO(Loan takeLoan);
}
