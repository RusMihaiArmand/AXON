package ro.axon.dot.model;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshRequest {

  @NotNull
  private String refreshToken;

}
