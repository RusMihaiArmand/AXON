package ro.axon.dot.model;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;

@Data
public class VacationDaysModifyDetails {

  @NotEmpty(message = "At least 1 employee id must be given")
  private List<String> employeeIds;

  @Min(value = 1, message = "Number of days must be greater than zero")
  private int noDays;
  @NotNull(message = "Vacation days change type cannot be null")
  private VacationDaysChangeTypeEnum type;
  @Size(max = 255)
  private String description;

}
