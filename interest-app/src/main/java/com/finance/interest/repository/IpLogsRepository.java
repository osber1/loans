package com.finance.interest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.interest.model.IpLogs;

@Repository
public interface IpLogsRepository extends JpaRepository<IpLogs, Integer> {

    Optional<IpLogs> findByIp(String ip);
}
