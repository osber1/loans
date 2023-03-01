package io.osvaldas.api.util;

public final class ExceptionMessages {

    public static final String CLIENT_NOT_FOUND = "Client with id %s does not exist.";

    public static final String LOAN_NOT_FOUND = "Loan with id %s does not exist.";

    public static final String LOAN_NOT_OPEN = "Loan with id %s is not open.";

    public static final String CLIENT_NOT_ACTIVE = "Client is not active.";

    public static final String CLIENT_ALREADY_EXIST = "Client with personal code already exists.";

    public static final String RISK_TOO_HIGH = "Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!";

    public static final String AMOUNT_EXCEEDS = "The amount you are trying to borrow exceeds the max amount!";

    public static final String LOAN_LIMIT_EXCEEDS = "Too many loans taken in a single day.";

    private ExceptionMessages() {
    }

}
