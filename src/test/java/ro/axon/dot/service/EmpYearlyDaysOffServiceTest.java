package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_END_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_START_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CRT_TMS;
import static ro.axon.dot.EmployeeTestAttributes.CRT_USR;
import static ro.axon.dot.EmployeeTestAttributes.EMAIL;
import static ro.axon.dot.EmployeeTestAttributes.FIRST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.LAST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.MDF_TMS;
import static ro.axon.dot.EmployeeTestAttributes.MDF_USR;
import static ro.axon.dot.EmployeeTestAttributes.ROLE;
import static ro.axon.dot.EmployeeTestAttributes.STATUS;
import static ro.axon.dot.EmployeeTestAttributes.TEAM_ETY;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmpYearlyDaysOffRepository;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsListItem;
import ro.axon.dot.model.VacationDaysModifyDetails;


@ExtendWith(MockitoExtension.class)
class EmpYearlyDaysOffServiceTest {


  @Mock
  EmpYearlyDaysOffRepository empYearlyDaysOffRepository;

  EmpYearlyDaysOffService empYearlyDaysOffService;

  @Mock
  EmployeeRepository employeeRepository;

  EmpYearlyDaysOffEty yearlyDaysOff1 = new EmpYearlyDaysOffEty();
  EmpYearlyDaysOffEty yearlyDaysOff2 = new EmpYearlyDaysOffEty();

  @BeforeEach
  void setUp() {
    empYearlyDaysOffService = new EmpYearlyDaysOffService(empYearlyDaysOffRepository);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);


    EmployeeEty employee = new EmployeeEty();
    employee.setId("id1");
    employee.setFirstName(FIRST_NAME);
    employee.setLastName(LAST_NAME);
    employee.setEmail(EMAIL);
    employee.setCrtUsr(CRT_USR);
    employee.setCrtTms(CRT_TMS);
    employee.setMdfUsr(MDF_USR);
    employee.setMdfTms(MDF_TMS);
    employee.setRole(ROLE);
    employee.setStatus(STATUS);
    employee.setContractStartDate(CONTRACT_START_DATE);
    employee.setContractEndDate(CONTRACT_END_DATE);
    employee.setUsername(USERNAME);
    employee.setTeam(TEAM_ETY);


    EmployeeEty employee2 = new EmployeeEty();
    employee2.setId("id2");
    employee2.setFirstName(FIRST_NAME);
    employee2.setLastName(LAST_NAME);
    employee2.setEmail("alt-mail");
    employee2.setCrtUsr(CRT_USR);
    employee2.setCrtTms(CRT_TMS);
    employee2.setMdfUsr(MDF_USR);
    employee2.setMdfTms(MDF_TMS);
    employee2.setRole(ROLE);
    employee2.setStatus(STATUS);
    employee2.setContractStartDate(CONTRACT_START_DATE);
    employee2.setContractEndDate(CONTRACT_END_DATE);
    employee2.setUsername("utilizator");
    employee2.setTeam(TEAM_ETY);

    employeeRepository.save(employee);
    employeeRepository.save(employee2);

    yearlyDaysOff1.setTotalNoDays(15);
    yearlyDaysOff1.setYear(2023);
    yearlyDaysOff1.setEmployeeId("id1");
    yearlyDaysOff1.setId(1L);

    yearlyDaysOff2.setTotalNoDays(25);
    yearlyDaysOff2.setYear(2023);
    yearlyDaysOff2.setEmployeeId("id2");

    empYearlyDaysOffRepository.save(yearlyDaysOff1);
    empYearlyDaysOffRepository.save(yearlyDaysOff2);

    List<EmpYearlyDaysOffEty> yearlyDaysOffDetailsList = new ArrayList<>();
    yearlyDaysOffDetailsList.add(yearlyDaysOff1);
    yearlyDaysOffDetailsList.add(yearlyDaysOff2);

    when(empYearlyDaysOffRepository.findAll()).thenReturn(yearlyDaysOffDetailsList);
  }

  @Test
  void changeVacationDays()
  {
    VacationDaysModifyDetails v = new VacationDaysModifyDetails();

    v.setNoDays(1);
    v.setType(VacationDaysChangeTypeEnum.INCREASE);
    v.setDescription("d");
    List<String> idList = new ArrayList<>();
    idList.add("id1");
    v.setEmployeeIds( idList );

    EmpYearlyDaysOffDetailsListItem daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id1",2023).getItems().get(0);
    assertEquals(15,daysOff.getTotalNoDays());

    int result = empYearlyDaysOffService.changeVacationDays(v);
    assertEquals(204,result);

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id1",2023).getItems().get(0);
    assertEquals(16,daysOff.getTotalNoDays());


    idList.add("id2");
    v.setEmployeeIds( idList );
    v.setNoDays(10);
    result = empYearlyDaysOffService.changeVacationDays(v);
    assertEquals(204,result);

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id1",2023).getItems().get(0);
    assertEquals(26,daysOff.getTotalNoDays());

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id2",2023).getItems().get(0);
    assertEquals(35,daysOff.getTotalNoDays());


    v.setType(VacationDaysChangeTypeEnum.DECREASE);
    result = empYearlyDaysOffService.changeVacationDays(v);
    assertEquals(204,result);

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id1",2023).getItems().get(0);
    assertEquals(16,daysOff.getTotalNoDays());

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id2",2023).getItems().get(0);
    assertEquals(25,daysOff.getTotalNoDays());


    v.setNoDays(20);
    result = empYearlyDaysOffService.changeVacationDays(v);
    assertEquals(400,result);

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id1",2023).getItems().get(0);
    assertEquals(-4,daysOff.getTotalNoDays());

    daysOff = empYearlyDaysOffService.getEmployeesYearlyDaysOffDetails("id2",2023).getItems().get(0);
    assertEquals(5,daysOff.getTotalNoDays());
  }
}