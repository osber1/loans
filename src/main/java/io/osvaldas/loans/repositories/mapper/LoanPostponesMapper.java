package io.osvaldas.loans.repositories.mapper;

import org.mapstruct.Mapper;

import io.osvaldas.loans.infra.rest.postpones.dtos.LoanPostponeResponse;
import io.osvaldas.loans.repositories.entities.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanPostponesMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}