package io.osvaldas.api.annotations

import jakarta.validation.ConstraintValidatorContext
import spock.lang.Specification
import spock.lang.Subject

class MobilePhoneValidatorSpec extends Specification {

    @Subject
    MobilePhoneValidator validator = new MobilePhoneValidator('^370\\d{8}$')

    void 'should check mobile phone number'() {
        expect:
            validator.isValid(phoneNumber, Stub(ConstraintValidatorContext)) == result
        where:
            phoneNumber   || result
            null          || false
            ''            || false
            'phoneNumber' || false
            '11111111111' || false
            '3701111111'  || false
            '37011111111' || true
    }

}
