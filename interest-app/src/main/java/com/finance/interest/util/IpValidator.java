package com.finance.interest.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.interfaces.IpValidationRule;
import com.finance.interest.interfaces.TimeUtils;
import com.finance.interest.model.IpLog;
import com.finance.interest.repository.IpLogsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidator implements IpValidationRule {

    private static final String TOO_MANY_REQUESTS = "Too many requests from the same ip per day.";

    private final IpLogsRepository ipLogsRepository;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    @Transactional
    public void validate(String ip) {
        checkIpAddress(ip, config.getRequestsFromSameIpLimit());
    }

    private void checkIpAddress(String ipAddress, int requestsFromSameIpLimit) {
        IpLog ipLog = ipLogsRepository.findByIp(ipAddress).orElseGet(() -> createIpLog(ipAddress));
        if (ipLog.getTimesUsed() >= requestsFromSameIpLimit && !checkIfItsTheNextDay(ipLog)) {
            throw new IpException(TOO_MANY_REQUESTS);
        } else {
            ipLog.setTimesUsed(ipLog.getTimesUsed() + 1);
            ipLogsRepository.save(ipLog);
        }
    }

    private boolean checkIfItsTheNextDay(IpLog ipFromDatabase) {
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

    private IpLog createIpLog(String ipAddress) {
        IpLog newIpToSave = IpLog.builder()
            .ip(ipAddress)
            .firstRequestDate(timeUtils.getCurrentDateTime()).build();
        return ipLogsRepository.save(newIpToSave);
    }

    static class IpException extends ValidationRuleException {

        public IpException(String message) {
            super(message);
        }
    }
}
