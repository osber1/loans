package com.finance.interest.util;

import static com.finance.interest.util.TimeUtils.getCurrentDateTime;
import static com.finance.interest.util.TimeUtils.getDayOfMonth;
import static com.finance.interest.util.TimeUtils.getHourOfDay;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.finance.interest.exception.BadRequestException;
import com.finance.interest.model.Client;
import com.finance.interest.model.IpLogs;
import com.finance.interest.repository.IpLogsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationUtils {

    private static final String RISK_MESSAGE = "Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!";

    private static final String AMOUNT_EXCEEDS = "The amount you are trying to borrow exceeds the max amount!";

    private static final String TOO_MANY_REQUESTS = "Too many requests from the same ip per day.";

    private final IpLogsRepository ipLogsRepository;

    public void checkTimeAndAmount(Client client, BigDecimal maxAmount, int forbiddenHourFrom, int forbiddenHourTo) {
        int currentHour = getHourOfDay();
        BigDecimal amount = client.getLoan().getAmount();
        if (forbiddenHourFrom <= currentHour && currentHour <= forbiddenHourTo && amount.compareTo(maxAmount) == 0) {
            throw new BadRequestException(RISK_MESSAGE);
        }
    }

    public void checkIfAmountIsNotToHigh(Client client, BigDecimal maxAmount) {
        int i = client.getLoan().getAmount().compareTo(maxAmount);
        if (i > 0) {
            throw new BadRequestException(AMOUNT_EXCEEDS);
        }
    }

    @Transactional
    public void checkIpAddress(HttpServletRequest request, int requestsFromSameIpLimit) {
        String ipAddress = request.getRemoteAddr();
        Optional<IpLogs> ipFromDatabase = ipLogsRepository.findByIp(ipAddress);
        if (ipFromDatabase.isEmpty()) {
            IpLogs newIpToSave = IpLogs.builder()
                .ip(ipAddress)
                .firstRequestDate(getCurrentDateTime())
                .timesUsed(1).build();
            ipLogsRepository.save(newIpToSave);
        } else {
            IpLogs ipLog = ipFromDatabase.get();
            if (ipLog.getTimesUsed() >= requestsFromSameIpLimit && !checkIfItsTheNextDay(ipLog)) {
                throw new BadRequestException(TOO_MANY_REQUESTS);
            } else {
                ipLog.setTimesUsed(ipLog.getTimesUsed() + 1);
                ipLogsRepository.save(ipLog);
            }
        }
    }

    public boolean checkIfItsTheNextDay(IpLogs ipFromDatabase) {
        ZonedDateTime currentDay = getDayOfMonth();
        ZonedDateTime lastRequestDay = ipFromDatabase.getFirstRequestDate().truncatedTo(ChronoUnit.DAYS);
        if (currentDay.isAfter(lastRequestDay)) {
            ipFromDatabase.setTimesUsed(0);
            ipLogsRepository.save(ipFromDatabase);
            return true;
        } else {
            return false;
        }
    }
}
