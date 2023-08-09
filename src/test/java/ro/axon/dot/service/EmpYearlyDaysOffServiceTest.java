package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_END_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_START_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CRT_TMS;
import static ro.axon.dot.EmployeeTestAttributes.CRT_USR;
import static ro.axon.dot.EmployeeTestAttributes.EMAIL;
import static ro.axon.dot.EmployeeTestAttributes.FIRST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.ID;
import static ro.axon.dot.EmployeeTestAttributes.LAST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.MDF_TMS;
import static ro.axon.dot.EmployeeTestAttributes.MDF_USR;
import static ro.axon.dot.EmployeeTestAttributes.ROLE;
import static ro.axon.dot.EmployeeTestAttributes.STATUS;
import static ro.axon.dot.EmployeeTestAttributes.TEAM_ETY;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmpYearlyDaysOffRepository;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsList;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
//TO ADD
@ExtendWith(MockitoExtension.class)
class EmpYearlyDaysOffServiceTest {

  EmployeeService employeeService;

  @Mock
  EmployeeRepository employeeRepository;


  EmpYearlyDaysOffService empYearlyDaysOffService;
  @Mock
  EmpYearlyDaysOffRepository empYearlyDaysOffRepository;

  @BeforeEach
  void setUp() {
    employeeService = new EmployeeService(employeeRepository);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);

    empYearlyDaysOffService = new EmpYearlyDaysOffService(empYearlyDaysOffRepository);
  }


  @Test
  void changeVacationDays()
  {

    EmpYearlyDaysOffEty empYearlyDaysOffEty = new EmpYearlyDaysOffEty();
    empYearlyDaysOffEty.setEmployeeId("1");
    empYearlyDaysOffEty.setTotalNoDays(22);
    empYearlyDaysOffEty.setId(1L);
    empYearlyDaysOffEty.setYear(2023);

    List<EmpYearlyDaysOffEty> empYearlyDaysOffEties = Arrays.asList(empYearlyDaysOffEty);

    when(empYearlyDaysOffRepository.findAll()).thenReturn(empYearlyDaysOffEties);

    EmpYearlyDaysOffDetailsList empYearlyDaysOffDetailsList =
        empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails();

    System.out.println(empYearlyDaysOffDetailsList.getItems().get(0).getYear());

    empYearlyDaysOffEty.setYear(2059);
    System.out.println(empYearlyDaysOffDetailsList.getItems().get(0).getYear());


  }
}