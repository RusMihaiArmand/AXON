package ro.axon.dot.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ro.axon.dot.model.LeaveRequestReview;

public class RejectionReasonRequiredValidator implements ConstraintValidator<RejectionReasonRequired, LeaveRequestReview> {

  @Override
  public boolean isValid(LeaveRequestReview value, ConstraintValidatorContext context) {

    if(value == null || value.getType() == null){
      return true;
    }

    if(value.getType().equals("REJECTION")){
      return value.getRejectionReason() != null && !value.getRejectionReason().isEmpty();
    }
    return true;
  }
}
