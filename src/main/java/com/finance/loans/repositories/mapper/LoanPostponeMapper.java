package com.finance.loans.repositories.mapper;

import org.mapstruct.Mapper;

import com.finance.loans.infra.rest.dtos.LoanPostponeResponse;
import com.finance.loans.repositories.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanPostponeMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}