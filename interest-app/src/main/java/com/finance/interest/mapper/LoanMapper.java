package com.finance.interest.mapper;

import java.util.Collection;

import com.finance.interest.LoanResponse;
import com.finance.interest.model.Loan;

@org.mapstruct.Mapper(componentModel = "spring")
public interface LoanMapper {

    Collection<LoanResponse> loanToDTOs(Collection<Loan> all);
}
