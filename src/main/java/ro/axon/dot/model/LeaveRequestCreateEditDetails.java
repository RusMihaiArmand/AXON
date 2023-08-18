package ro.axon.dot.model;

import java.time.LocalDate;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;

public interface LeaveRequestCreateEditDetails {
  LocalDate getStartDate();
  LocalDate getEndDate();
  LeaveRequestEtyTypeEnum getType();
  String getDescription();
}
