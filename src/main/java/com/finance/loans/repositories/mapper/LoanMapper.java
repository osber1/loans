package com.finance.loans.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.finance.loans.infra.rest.loans.dtos.LoanRequest;
import com.finance.loans.infra.rest.loans.dtos.LoanResponse;
import com.finance.loans.repositories.entities.Loan;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    Collection<LoanResponse> loanToDTOs(Collection<Loan> all);

    Loan loanToEntity(LoanRequest request);

    LoanResponse loanToDTO(Loan takeLoan);
}
