package ro.axon.dot.model;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;

@Data
public class VacationDaysModifyDetails {

  @NotEmpty(message = "At least 1 employee id must be given")
  private List<String> employeeIds;


  @Min(value = 1, message = "Numbers of days must be greater than zero")
  private int noDays;

  private VacationDaysChangeTypeEnum type;

  private String description;


}
