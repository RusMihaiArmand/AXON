package ro.axon.dot.model;

import java.time.LocalDate;
import lombok.Data;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;

@Data
public class CreateLeaveRequestDetails implements LeaveRequestCreateEditDetails {

  private LocalDate startDate;
  private LocalDate endDate;
  private LeaveRequestEtyTypeEnum type;
  private String description;


}
