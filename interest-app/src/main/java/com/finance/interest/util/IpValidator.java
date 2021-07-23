package com.finance.interest.util;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.exception.BadRequestException;
import com.finance.interest.interfaces.TimeUtils;
import com.finance.interest.interfaces.ValidationRule;
import com.finance.interest.model.IpLogs;
import com.finance.interest.repository.IpLogsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidator implements ValidationRule {

    private static final String TOO_MANY_REQUESTS = "Too many requests from the same ip per day.";

    private final IpLogsRepository ipLogsRepository;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    @Transactional
    public void validate(String ip, BigDecimal clientAmount) {
        checkIpAddress(ip, config.getRequestsFromSameIpLimit());
    }

    private void checkIpAddress(String ipAddress, int requestsFromSameIpLimit) {
        IpLogs ipLog = ipLogsRepository.findByIp(ipAddress).orElseGet(() -> createIpLogs(ipAddress));
        if (ipLog.getTimesUsed() >= 1) {
            if (ipLog.getTimesUsed() >= requestsFromSameIpLimit && !checkIfItsTheNextDay(ipLog)) {
                throw new BadRequestException(TOO_MANY_REQUESTS);
            } else {
                ipLog.setTimesUsed(ipLog.getTimesUsed() + 1);
                ipLogsRepository.save(ipLog);
            }
        }
    }

    private boolean checkIfItsTheNextDay(IpLogs ipFromDatabase) {
        ZonedDateTime currentDay = timeUtils.getDayOfMonth();
        ZonedDateTime lastRequestDay = ipFromDatabase.getFirstRequestDate().truncatedTo(ChronoUnit.DAYS);
        if (currentDay.isAfter(lastRequestDay.plusDays(1))) {
            ipFromDatabase.setTimesUsed(0);
            ipLogsRepository.save(ipFromDatabase);
            return true;
        } else {
            return false;
        }
    }

    private IpLogs createIpLogs(String ipAddress) {
        IpLogs newIpToSave = IpLogs.builder()
            .ip(ipAddress)
            .firstRequestDate(timeUtils.getCurrentDateTime())
            .timesUsed(1).build();
        return ipLogsRepository.save(newIpToSave);
    }
}
