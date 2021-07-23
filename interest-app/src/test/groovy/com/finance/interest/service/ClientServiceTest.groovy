package com.finance.interest.service

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.repository.ClientRepository
import com.finance.interest.repository.LoanRepository
import com.finance.interest.util.RiskValidator
import com.finance.interest.util.TimeService

import spock.lang.Specification

class ClientServiceTest extends Specification {

    ClientRepository clientRepository = Mock()

    LoanRepository loanRepository = Mock()

    PropertiesConfig config = Mock()

    RiskValidator validator = Mock()

    TimeService timeService = Mock()

    ClientService clientService = new ClientService(clientRepository, loanRepository, config, validator, timeService)

    void 'should work when run'() {
//        given:
//
//        when:
//            clientService.takeLoan()
//        then:
    }

}