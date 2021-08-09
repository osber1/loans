package com.finance.interest.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
//@RedisHash("IpLog")
public class IpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String ip;

    private int timesUsed;

    private ZonedDateTime firstRequestDate;

    public static IpLog buildNewIpLog(String ipAddress, ZonedDateTime currentDateTime) {
        IpLog ipLog = new IpLog();
        ipLog.setIp(ipAddress);
        ipLog.setFirstRequestDate(currentDateTime);
        return ipLog;
    }
}
