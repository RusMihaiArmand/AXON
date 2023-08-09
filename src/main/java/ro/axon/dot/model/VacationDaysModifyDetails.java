package ro.axon.dot.model;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;

@Data
public class VacationDaysModifyDetails {

  @NotEmpty
  private List<String> employeeIds;

  @NotEmpty
  private int noDays;

  @NotEmpty
  private VacationDaysChangeTypeEnum type;

  private String description;


}
