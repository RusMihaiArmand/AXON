package ro.axon.dot.service;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmpYearlyDaysOffRepository;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.mapper.EmpYearlyDaysOffMapper;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsList;
import ro.axon.dot.model.VacationDaysModifyDetails;

@Service
@RequiredArgsConstructor
public class EmpYearlyDaysOffService {

  private final EmpYearlyDaysOffRepository empYearlyDaysOffRepository;

  public EmpYearlyDaysOffDetailsList getEmployeesYearlyDaysOffDetails(String id, int year) {
    var EmpYearlyDaysOffDetailsList = new EmpYearlyDaysOffDetailsList();

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEties;



    if (year>0) {
      empYearlyDaysOffEties = empYearlyDaysOffRepository.findAll().stream()
          .filter( empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getEmployeeId().equals(id))
          .filter(empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getYear().equals(year))
          .collect(Collectors.toList());
    } else {
      empYearlyDaysOffEties = empYearlyDaysOffRepository.findAll().stream()
          .filter( empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getEmployeeId().equals(id))
          .collect(Collectors.toList());
    }
    EmpYearlyDaysOffDetailsList.setItems(empYearlyDaysOffEties.stream()
        .map(EmpYearlyDaysOffMapper.INSTANCE::mapEmpYearlyDaysOffToEmpYearlyDaysOffDto)
        .collect(Collectors.toList()));

    return EmpYearlyDaysOffDetailsList;




//    List<EmployeeEty> employees;
//
//    Optional<String> searchName = Optional.ofNullable(name);
//
//    if (searchName.isPresent() && !searchName.get().isEmpty()) {
//      employees = employeeRepository.findAll().stream()
//          .filter(employee ->
//              employee.getFirstName().toLowerCase().contains(searchName.get().toLowerCase()) ||
//              employee.getLastName().toLowerCase().contains(searchName.get().toLowerCase()))
//          .collect(Collectors.toList());
//    } else {
//      employees = employeeRepository.findAll();
//    }
//
//    employeeDetailsList.setItems(employees.stream()
//        .map(EmployeeMapper.INSTANCE::mapEmployeeEtyToEmployeeDto)
//        .collect(Collectors.toList()));
//
//    return employeeDetailsList;
  }

  public EmpYearlyDaysOffDetailsList getEmployeesYearlyDaysOffDetails() {
    var EmpYearlyDaysOffDetailsList = new EmpYearlyDaysOffDetailsList();

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEties;


      empYearlyDaysOffEties = empYearlyDaysOffRepository.findAll().stream()
          .collect(Collectors.toList());

    EmpYearlyDaysOffDetailsList.setItems(empYearlyDaysOffEties.stream()
        .map(EmpYearlyDaysOffMapper.INSTANCE::mapEmpYearlyDaysOffToEmpYearlyDaysOffDto)
        .collect(Collectors.toList()));

    return EmpYearlyDaysOffDetailsList;

  }


  public int changeVacationDays(VacationDaysModifyDetails vacationDaysModifyDetails)
  {

    boolean daysWentNegative = false;
    int currentYear = Year.now().getValue();

    int dayChanger = vacationDaysModifyDetails.getNoDays();
    if(vacationDaysModifyDetails.getType().equals(VacationDaysChangeTypeEnum.DECREASE))
      dayChanger = -dayChanger;



    EmpYearlyDaysOffEty empYearlyDaysOffEty;

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEties;

    for (String empId: vacationDaysModifyDetails.getEmployeeIds()) {

      empYearlyDaysOffEties = null;

      empYearlyDaysOffEties = empYearlyDaysOffRepository.findAll().stream()
          .filter(ety -> ety.getEmployeeId().equals(empId))
          .filter(ety -> ety.getYear().equals(currentYear))
          .collect(Collectors.toList());

      if(!(empYearlyDaysOffEties == null || empYearlyDaysOffEties.size()==0))
      {
        empYearlyDaysOffEty = empYearlyDaysOffEties.get(0);

        empYearlyDaysOffEty.setTotalNoDays( empYearlyDaysOffEty.getTotalNoDays() + dayChanger  );

        if(empYearlyDaysOffEty.getTotalNoDays()<0)
          daysWentNegative = true;

        empYearlyDaysOffRepository.save(empYearlyDaysOffEty);

      }

    }



    //EmployeeMapper.INSTANCE.mapEmployeeEtyToEmployeeDto(employeeRepository.save(employeeEty));

    if(daysWentNegative)
      return 400;
    else
      return 204;
  }



}



