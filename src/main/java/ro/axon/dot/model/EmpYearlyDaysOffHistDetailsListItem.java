package ro.axon.dot.model;

import java.time.Instant;
import lombok.Data;

@Data
public class EmpYearlyDaysOffHistDetailsListItem {

  private Long id;
  private Integer noDays;
  private String description;
  private String crtUsr;
  private Instant crtTms;

}
