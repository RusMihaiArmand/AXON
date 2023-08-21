package ro.axon.dot.model;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import ro.axon.dot.domain.enums.LeaveRequestType;
import ro.axon.dot.validation.DateRangeConstraint;

@Data
@DateRangeConstraint
public class EditLeaveRequestDetails implements LeaveRequestCreateEditDetails {

  private LocalDate startDate;
  private LocalDate endDate;
  @NotNull(message = "Leave Request type cannot be null")
  private LeaveRequestType type;
  @Size(max = 255, message = "Description cannot exceed 255 characters")
  private String description;
  @NotNull(message = "Version cannot be null")
  private Long v;
}
