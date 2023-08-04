package ro.axon.dot.mapper;

import java.util.Calendar;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.model.EmployeeDetailsListItem;

/**
 * Mapper used for converting EmployeeEty object to EmployeeDto object
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeMapper {

  EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

  @Mapping(source = "team", target = "teamDetails")
  @Mapping(target = "totalVacationDays", expression = "java(mapTotalVacationDays(employeeEty.getEmpYearlyDaysOff()))")
  EmployeeDetailsListItem mapEmployeeEtyToEmployeeDto(EmployeeEty employeeEty);

  default Integer mapTotalVacationDays(Set<EmpYearlyDaysOffEty> empYearlyDaysOff) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    return empYearlyDaysOff.stream()
        .filter(daysOff -> daysOff.getYear().equals(currentYear))
        .findFirst()
        .map(EmpYearlyDaysOffEty::getTotalNoDays)
        .orElse(0);
  }
}
