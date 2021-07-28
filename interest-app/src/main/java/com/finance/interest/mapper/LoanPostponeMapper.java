package com.finance.interest.mapper;

import org.mapstruct.Mapper;

import com.finance.interest.LoanPostponeResponse;
import com.finance.interest.model.LoanPostpone;

@Mapper(componentModel = "spring")
public interface LoanPostponeMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}