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

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEtyList;


    if (year>0) {
      empYearlyDaysOffEtyList = empYearlyDaysOffRepository.findAll().stream()
          .filter( empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getEmployeeId().equals(id))
          .filter(empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getYear().equals(year))
          .collect(Collectors.toList());
    } else {
      empYearlyDaysOffEtyList = empYearlyDaysOffRepository.findAll().stream()
          .filter( empYearlyDaysOffEty ->
              empYearlyDaysOffEty.getEmployeeId().equals(id))
          .collect(Collectors.toList());
    }
    EmpYearlyDaysOffDetailsList.setItems(empYearlyDaysOffEtyList.stream()
        .map(EmpYearlyDaysOffMapper.INSTANCE::mapEmpYearlyDaysOffToEmpYearlyDaysOffDto)
        .collect(Collectors.toList()));

    return EmpYearlyDaysOffDetailsList;

  }

  public EmpYearlyDaysOffDetailsList getEmployeesYearlyDaysOffDetails() {
    var EmpYearlyDaysOffDetailsList = new EmpYearlyDaysOffDetailsList();

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEtyList = empYearlyDaysOffRepository.findAll().stream()
          .toList();

    EmpYearlyDaysOffDetailsList.setItems(empYearlyDaysOffEtyList.stream()
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

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEtyList;

    for (String empId: vacationDaysModifyDetails.getEmployeeIds()) {

      empYearlyDaysOffEtyList = empYearlyDaysOffRepository.findAll().stream()
          .filter(ety -> ety.getEmployeeId().equals(empId))
          .filter(ety -> ety.getYear().equals(currentYear))
          .collect(Collectors.toList());

      if(empYearlyDaysOffEtyList.size()!=0)
      {
        empYearlyDaysOffEty = empYearlyDaysOffEtyList.get(0);

        empYearlyDaysOffEty.setTotalNoDays( empYearlyDaysOffEty.getTotalNoDays() + dayChanger  );

        if(empYearlyDaysOffEty.getTotalNoDays()<0)
          daysWentNegative = true;

        empYearlyDaysOffRepository.save(empYearlyDaysOffEty);

      }

    }

    if(daysWentNegative)
      return 400;
    else
      return 204;
  }

}



