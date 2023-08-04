package ro.axon.dot.model;

import java.time.Instant;
import lombok.Data;

@Data
public class EmployeeDetailsListItem {

  private String id;

  private String firstName;
  private String lastName;
  private String email;
  private String crtUsr;
  private Instant crtTms;
  private String mdfUsr;
  private Instant mdfTms;
  private String role;
  private String status;
  private Instant contractStartDate;
  private Long v;
  private Integer totalVacationDays;
  private TeamDetailsListItem teamDetails;
  private String username;

}
