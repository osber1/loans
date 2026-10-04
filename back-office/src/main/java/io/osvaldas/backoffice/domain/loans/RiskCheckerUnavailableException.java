package io.osvaldas.backoffice.domain.loans;

public class RiskCheckerUnavailableException extends RuntimeException {

    public RiskCheckerUnavailableException(Throwable cause) {
        super("Risk checker is unavailable", cause);
    }

}
