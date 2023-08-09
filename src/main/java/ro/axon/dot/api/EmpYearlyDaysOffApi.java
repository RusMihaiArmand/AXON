package ro.axon.dot.api;

import java.util.ArrayList;
import java.util.List;
import liquibase.pro.packaged.S;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsList;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.VacationDaysModifyDetails;
import ro.axon.dot.service.EmpYearlyDaysOffService;
import ro.axon.dot.service.EmployeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmpYearlyDaysOffApi {

  private final EmpYearlyDaysOffService empYearlyDaysOffService;

  @GetMapping(value = {"/employees-yearly-days-off/{id}"})
  public ResponseEntity<EmpYearlyDaysOffDetailsList> getEmployeesList(@RequestParam String id){


    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails(id,0);
    return ResponseEntity.ok(empYearlyDaysOffDetailsList);
  }



  @GetMapping(value = {"/yearly-days-off"})
  public ResponseEntity<EmpYearlyDaysOffDetailsList> getEmployeesList2(){

    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails();
    return ResponseEntity.ok(empYearlyDaysOffDetailsList);
  }


  @PostMapping("/employees/days-off")
  public ResponseEntity<Void> modifyEmployeesDaysOff(@RequestBody VacationDaysModifyDetails vacationDaysModifyDetails)
  {
    int response = empYearlyDaysOffService.changeVacationDays(vacationDaysModifyDetails);

    if(response==400)
    {
      return ResponseEntity.status(400).build();
    }

    return ResponseEntity.status(204).build();
  }

  @PostMapping("/poster")
  public void postTerster()
  {
    System.out.println("TEST FOR POST");
  }


  @GetMapping("/days-test")
  public ResponseEntity<Void> modifyEmployeesDaysOff()
  {

    VacationDaysModifyDetails vacationDaysModifyDetails = new VacationDaysModifyDetails();

    List<String> lista = new ArrayList<>();
    lista.add("3");lista.add("4");
    vacationDaysModifyDetails.setEmployeeIds(lista);

    vacationDaysModifyDetails.setDescription("d");
    vacationDaysModifyDetails.setNoDays(3);
    vacationDaysModifyDetails.setType(VacationDaysChangeTypeEnum.INCREASE);

    int response = empYearlyDaysOffService.changeVacationDays(vacationDaysModifyDetails);

    if(response==400)
    {
      return ResponseEntity.status(400).build();
    }

    return ResponseEntity.status(204).build();
  }

}
