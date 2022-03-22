package com.finance.loans.repositories.mapper;

import org.mapstruct.Mapper;

import com.finance.loans.infra.rest.loans.dtos.LoanPostponeResponse;
import com.finance.loans.repositories.entities.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanPostponeMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}