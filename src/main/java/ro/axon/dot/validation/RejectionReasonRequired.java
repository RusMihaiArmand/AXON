package ro.axon.dot.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RejectionReasonRequiredValidator.class)
public @interface RejectionReasonRequired {

  String message() default "Rejection reason is required for rejected leave requests.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
