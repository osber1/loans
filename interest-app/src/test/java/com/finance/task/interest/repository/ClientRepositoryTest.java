package com.finance.task.interest.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.finance.interest.model.ClientDAO;
import com.finance.interest.model.Loan;
import com.finance.interest.repository.ClientRepository;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository underTest;

    private Optional<ClientDAO> databaseResponse;

    @BeforeEach
    void init() {
        Loan loan = new Loan();
        loan.setId(1);
        loan.setInterestRate(10.0);
        loan.setTermInMonths(10);
        loan.setAmount(BigDecimal.valueOf(10));

        ClientDAO client = ClientDAO.builder()
            .id("ID-TO-TEST")
            .firstName("Test")
            .lastName("User")
            .personalCode(11111111111L)
            .loans(Collections.singleton(loan)).build();
        databaseResponse = Optional.of(underTest.save(client));
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void testFindById_whenExist() {
        // given
        String id = "ID-TO-TEST";

        // when
        Optional<ClientDAO> expected = underTest.findById(id);

        // then
        assertThat(expected).isEqualTo(databaseResponse);
    }

    @Test
    void testFindById_whenNotExist() {
        // given
        String id = "NON-EXISTING-ID";

        // when
        Optional<ClientDAO> expected = underTest.findById(id);

        // then
        assertThat(expected).isNotPresent();
    }

    @Test
    void testFindByPersonalCode_whenExist() {
        // given
        long personalCode = 11111111111L;

        // when
        Optional<ClientDAO> expected = underTest.findByPersonalCode(personalCode);

        // then
        assertThat(expected).isEqualTo(databaseResponse);
    }

    @Test
    void testFindByPersonalCode_whenNotExist() {
        // given
        long personalCode = 11111111110L;

        // when
        Optional<ClientDAO> expected = underTest.findByPersonalCode(personalCode);

        // then
        assertThat(expected).isNotPresent();
    }
}