package io.osvaldas.api.util;

public final class ExceptionMessages {

    public static final String CLIENT_NOT_FOUND = "Client with id %s does not exist.";

    public static final String LOAN_NOT_FOUND = "Loan with id %s does not exist.";

    public static final String LOAN_NOT_OPEN = "Loan with id %s is not open.";

    public static final String CLIENT_NOT_ACTIVE = "Client is not active.";

    public static final String CLIENT_ALREADY_EXIST = "Client with personal code already exists.";

    /**
     * Rendering of {@link #RISK_TOO_HIGH_BETWEEN_HOURS} for the default 00:00-6:00 window.
     *
     * @deprecated the forbidden window is configurable, build the message from {@link #RISK_TOO_HIGH_BETWEEN_HOURS}.
     */
    @Deprecated
    public static final String RISK_TOO_HIGH = "Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!";

    /**
     * Template taking the start (inclusive) and the end (exclusive) of the forbidden window, e.g. {@code "00:00"} and {@code "06:00"}.
     */
    public static final String RISK_TOO_HIGH_BETWEEN_HOURS = "Risk is too high, because you are trying to get loan between %s and %s and you want to borrow the max amount!";

    public static final String AMOUNT_EXCEEDS = "The amount you are trying to borrow exceeds the max amount!";

    public static final String LOAN_LIMIT_EXCEEDS = "Too many loans taken in a single day.";

    private ExceptionMessages() {
    }

}
