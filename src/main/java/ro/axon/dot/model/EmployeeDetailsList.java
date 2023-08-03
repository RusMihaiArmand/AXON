package ro.axon.dot.model;

import java.util.List;
import lombok.Data;

@Data
public class EmployeeDetailsList {
  private List<EmployeeDetailsListItem> items;
}
