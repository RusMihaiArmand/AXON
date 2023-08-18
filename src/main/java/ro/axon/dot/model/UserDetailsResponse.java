package ro.axon.dot.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {

  private String employeeId;
  private String username;
  private List<String> roles;
  private TeamDetails teamDetails;

}
