package io.osvaldas.backoffice.domain.loans;

import feign.FeignException;

public final class RiskCheckerErrors {

    private static final int SERVER_ERROR_FROM = 500;

    private RiskCheckerErrors() {
    }

    public static boolean isUnavailable(FeignException e) {
        return e.status() <= 0 || e.status() >= SERVER_ERROR_FROM;
    }

}
