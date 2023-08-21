package ro.axon.dot.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ro.axon.dot.domain.entity.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.model.EmployeeDetailsListItem;

class EmployeeMapperTest {

  EmployeeMapper employeeMapper = EmployeeMapper.INSTANCE;

  @Test
  void mapEmployeeEtyToEmployeeDto() {

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);

    EmpYearlyDaysOffEty empYearlyDaysOffEty = new EmpYearlyDaysOffEty();
    empYearlyDaysOffEty.setId(1L);
    empYearlyDaysOffEty.setTotalNoDays(21);
    empYearlyDaysOffEty.setYear(2023);

    Set<EmpYearlyDaysOffEty> empYearlyDaysOffEtySet = new HashSet<>();
    empYearlyDaysOffEtySet.add(empYearlyDaysOffEty);

    EmployeeEty employeeEty = new EmployeeEty();

    employeeEty.setId(ID);
    employeeEty.setFirstName(FIRST_NAME);
    employeeEty.setLastName(LAST_NAME);
    employeeEty.setEmail(EMAIL);
    employeeEty.setCrtUsr(CRT_USR);
    employeeEty.setCrtTms(CRT_TMS);
    employeeEty.setMdfUsr(MDF_USR);
    employeeEty.setMdfTms(MDF_TMS);
    employeeEty.setRole(ROLE);
    employeeEty.setStatus(STATUS);
    employeeEty.setContractStartDate(CONTRACT_START_DATE);
    employeeEty.setUsername(USERNAME);
    employeeEty.setTeam(TEAM_ETY);
    employeeEty.setEmpYearlyDaysOff(empYearlyDaysOffEtySet);


    EmployeeDetailsListItem employeeDetailsListItem = employeeMapper.mapEmployeeEtyToEmployeeDto(employeeEty);

    assertEquals(ID, employeeDetailsListItem.getId());
    assertEquals(FIRST_NAME, employeeDetailsListItem.getFirstName());
    assertEquals(LAST_NAME, employeeDetailsListItem.getLastName());
    assertEquals(EMAIL, employeeDetailsListItem.getEmail());
    assertEquals(CRT_USR, employeeDetailsListItem.getCrtUsr());
    assertEquals(CRT_TMS, employeeDetailsListItem.getCrtTms());
    assertEquals(MDF_USR, employeeDetailsListItem.getMdfUsr());
    assertEquals(MDF_TMS, employeeDetailsListItem.getMdfTms());
    assertEquals(ROLE, employeeDetailsListItem.getRole());
    assertEquals(STATUS, employeeDetailsListItem.getStatus());
    assertEquals(CONTRACT_START_DATE, employeeDetailsListItem.getContractStartDate());
    assertEquals(USERNAME, employeeDetailsListItem.getUsername());
    assertEquals(21, employeeDetailsListItem.getTotalVacationDays());

    assertEquals(TEAM_ETY.getId(), employeeDetailsListItem.getTeamDetails().getId());
    assertEquals(TEAM_ETY.getName(), employeeDetailsListItem.getTeamDetails().getName());
    assertEquals(TEAM_ETY.getCrtUsr(), employeeDetailsListItem.getTeamDetails().getCrtUsr());
    assertEquals(TEAM_ETY.getMdfUsr(), employeeDetailsListItem.getTeamDetails().getMdfUsr());
    assertEquals(TEAM_ETY.getMdfTms(), employeeDetailsListItem.getTeamDetails().getMdfTms());
  }
}