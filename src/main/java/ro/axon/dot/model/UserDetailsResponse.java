package ro.axon.dot.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsResponse {

  private String employeeId;
  private String username;
  private List<String> roles;
  private TeamDetails teamDetails;

}
