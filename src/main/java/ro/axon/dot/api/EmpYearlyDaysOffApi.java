package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsList;
import ro.axon.dot.model.VacationDaysModifyDetails;
import ro.axon.dot.service.EmpYearlyDaysOffService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmpYearlyDaysOffApi {

  private final EmpYearlyDaysOffService empYearlyDaysOffService;

  @GetMapping(value = {"/employees-yearly-days-off"})
  public ResponseEntity<EmpYearlyDaysOffDetailsList> getYearlyDaysOff(){
    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails();
    return ResponseEntity.ok(empYearlyDaysOffDetailsList);
  }

  @GetMapping(value = {"/employees-yearly-days-off/{id}"})
  public ResponseEntity<EmpYearlyDaysOffDetailsList> getYearlyDaysOff(@PathVariable String id){
    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails(id,0);
    return ResponseEntity.ok(empYearlyDaysOffDetailsList);
  }

  @GetMapping(value = {"/employees-yearly-days-off/{id}/{year}"})
  public ResponseEntity<EmpYearlyDaysOffDetailsList> getYearlyDaysOff(@PathVariable String id, @PathVariable int year)
  {
    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails(id,year);
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
}
