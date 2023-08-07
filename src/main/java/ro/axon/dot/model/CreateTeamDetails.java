package ro.axon.dot.model;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateTeamDetails {

  @NotEmpty(message = "Team name cannot be empty or null")
  private String name;

}
