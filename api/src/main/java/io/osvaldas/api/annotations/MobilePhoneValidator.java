package io.osvaldas.api.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;

public class MobilePhoneValidator implements ConstraintValidator<MobilePhone, String> {

    private final String countryCode;

    private final int numberLength;

    public MobilePhoneValidator(@Value("${validation.mobilePhone.countryCode:}") String countryCode,
                                @Value("${validation.mobilePhone.numberLength:}") int numberLength) {
        this.countryCode = countryCode;
        this.numberLength = numberLength;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.length() == numberLength && value.startsWith(countryCode);
    }

}
