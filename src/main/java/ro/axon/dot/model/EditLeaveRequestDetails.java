package ro.axon.dot.model;

import java.time.LocalDate;
import lombok.Data;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;

@Data
public class EditLeaveRequestDetails {

  private LocalDate startDate;
  private LocalDate endDate;
  private LeaveRequestEtyTypeEnum type;
  private String description;
  private Long v;
}
