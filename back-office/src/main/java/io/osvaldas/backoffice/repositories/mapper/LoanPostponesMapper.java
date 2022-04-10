package io.osvaldas.backoffice.repositories.mapper;

import org.mapstruct.Mapper;

import io.osvaldas.backoffice.infra.rest.postpones.dtos.LoanPostponeResponse;
import io.osvaldas.backoffice.repositories.entities.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanPostponesMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}
