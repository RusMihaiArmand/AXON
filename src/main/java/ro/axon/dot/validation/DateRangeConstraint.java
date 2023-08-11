package ro.axon.dot.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface DateRangeConstraint {
  String message() default "Start date must be before end date and cannot be older than the current month";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

}
