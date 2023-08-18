package ro.axon.dot.validation;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ro.axon.dot.model.LeaveRequestCreateEditDetails;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, LeaveRequestCreateEditDetails> {

  @Override
  public boolean isValid(LeaveRequestCreateEditDetails value, ConstraintValidatorContext context){

    if(value == null){
      return true; // nulls are taken care of by @NotNull annotations
    }

    LocalDate startDate = value.getStartDate();
    LocalDate endDate = value.getEndDate();

    if(startDate == null || endDate == null) {
      sentNullDateErrorMessage(context);
      return false;
    }

    return startDate.isBefore(endDate);
  }

  private void sentNullDateErrorMessage(ConstraintValidatorContext context){

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate("Date cannot be null")
        .addConstraintViolation();
  }
}
