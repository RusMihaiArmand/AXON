package ro.axon.dot.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDetails {
  private Long teamId;
  private String name;
}
