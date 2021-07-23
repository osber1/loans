package com.finance.interest.service

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.repository.ClientRepository
import com.finance.interest.repository.LoanRepository

import spock.lang.Specification

class ClientServiceTest extends Specification {

    ClientRepository clientRepository = Mock()

    LoanRepository loanRepository = Mock()

    PropertiesConfig config = Mock()

//    ValidationUtils validationUtils = Mock()

    ClientService clientService = new ClientService(clientRepository, loanRepository, config, validationUtils)

}