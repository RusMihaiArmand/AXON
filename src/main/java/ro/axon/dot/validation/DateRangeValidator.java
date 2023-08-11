package ro.axon.dot.validation;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ro.axon.dot.model.EditLeaveRequestDetails;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, EditLeaveRequestDetails> {

  @Override
  public boolean isValid(EditLeaveRequestDetails leaveRequest, ConstraintValidatorContext context){

    if(leaveRequest == null){
      return true; // nulls are taken care of by @NotNull annotations
    }

    LocalDate startDate = leaveRequest.getStartDate();
    LocalDate endDate = leaveRequest.getEndDate();

    if(startDate == null || endDate == null) {
      sentNullDateErrorMessage(context);
      return false;
    }

    return isValidDateRange(startDate, endDate);
  }

  private boolean isValidDateRange(LocalDate startDate, LocalDate endDate){
    return startDate.isBefore(endDate) && startDate.isAfter(LocalDate.now().withDayOfMonth(1));
  }

  private void sentNullDateErrorMessage(ConstraintValidatorContext context){

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate("Date cannot be null")
        .addConstraintViolation();
  }
}
