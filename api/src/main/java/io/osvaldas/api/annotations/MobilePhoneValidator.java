package io.osvaldas.api.annotations;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;

public class MobilePhoneValidator implements ConstraintValidator<MobilePhone, String> {

    private final Pattern pattern;

    public MobilePhoneValidator(@Value("${validation.mobilePhone.pattern:^370\\d{8}$}") String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && pattern.matcher(value).matches();
    }

}
