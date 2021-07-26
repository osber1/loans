package com.finance.interest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.interest.model.IpLog;

@Repository
public interface IpLogsRepository extends JpaRepository<IpLog, Integer> {

    Optional<IpLog> findByIp(String ip);
}
