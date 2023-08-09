package ro.axon.dot.model;

import lombok.Data;

@Data
public class EmpYearlyDaysOffDetailsListItem {
  private Long id;
  private Integer totalNoDays;
  private Integer year;
  private String employeeId;
}
