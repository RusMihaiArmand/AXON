package ro.axon.dot.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeamDetails {

  @NotNull
  @NotBlank
  private String name;

}
