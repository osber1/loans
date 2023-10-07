package io.osvaldas.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MobilePhoneValidator.class)
public @interface MobilePhone {

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "Wrong phone number provided";

}
