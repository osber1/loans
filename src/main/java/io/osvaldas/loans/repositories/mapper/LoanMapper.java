package io.osvaldas.loans.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.loans.infra.rest.loans.dtos.LoanRequest;
import io.osvaldas.loans.infra.rest.loans.dtos.LoanResponse;
import io.osvaldas.loans.repositories.entities.Loan;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    Collection<LoanResponse> loanToDTOs(Collection<Loan> all);

    Loan loanToEntity(LoanRequest request);

    LoanResponse loanToDTO(Loan takeLoan);
}
