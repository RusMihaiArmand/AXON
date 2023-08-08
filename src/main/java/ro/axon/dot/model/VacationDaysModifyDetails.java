package ro.axon.dot.model;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;

@Data
public class VacationDaysModifyDetails {

  //@NotEmpty(message = "At least 1 employee ID is required")
  private List<String> employeeIds;

  // @NotEmpty(message = "Number of days required")
  private int noDays;

  // @NotEmpty(message = "Type required")
  private VacationDaysChangeTypeEnum type;

  private String description;


}
