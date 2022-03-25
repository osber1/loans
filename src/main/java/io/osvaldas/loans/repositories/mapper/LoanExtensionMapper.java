package io.osvaldas.loans.repositories.mapper;

import org.mapstruct.Mapper;

import io.osvaldas.loans.infra.rest.extensions.dtos.LoanExtensionResponse;
import io.osvaldas.loans.repositories.entities.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanExtensionMapper {

    LoanExtensionResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}