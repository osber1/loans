package com.finance.interest.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.finance.interest.model.IpLogs;
import com.finance.interest.repository.IpLogsRepository;

@DataJpaTest
class IpLogsRepositoryTestOld {

    @Autowired
    private IpLogsRepository underTest;

    private Optional<IpLogs> databaseResponse;

    @BeforeEach
    void init() {
        IpLogs client = IpLogs.builder()
            .id(1)
            .ip("0.0.0.0.1")
            .timesUsed(1)
            .build();
        databaseResponse = Optional.of(underTest.save(client));
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void testFindByIp_whenExist() {
        // given
        String ip = "0.0.0.0.1";

        // when
        Optional<IpLogs> expected = underTest.findByIp(ip);

        // then
        assertThat(expected).isEqualTo(databaseResponse);
    }

    @Test
    void testFindByIp_whenNotExist() {
        // given
        String ip = "0.0.0.0.0";

        // when
        Optional<IpLogs> expected = underTest.findByIp(ip);

        // then
        assertThat(expected).isNotPresent();
    }
}