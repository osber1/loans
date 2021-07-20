package com.finance.interest.mapper;

import com.finance.interest.LoanPostponeResponse;
import com.finance.interest.model.LoanPostpone;

@org.mapstruct.Mapper(componentModel = "spring")
public interface LoanPostponeMapper {

    LoanPostponeResponse loanPostponeToDTO(LoanPostpone loanPostpone);
}
