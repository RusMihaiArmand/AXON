package ro.axon.dot.model;

import java.time.LocalDate;
import ro.axon.dot.domain.enums.LeaveRequestType;

public interface LeaveRequestCreateEditDetails {
  LocalDate getStartDate();
  LocalDate getEndDate();
  LeaveRequestType getType();
  String getDescription();
}
