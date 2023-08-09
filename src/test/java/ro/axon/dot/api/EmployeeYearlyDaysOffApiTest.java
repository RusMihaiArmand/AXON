package ro.axon.dot.api;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.axon.dot.EmployeeTestAttributes.CRT_TMS;
import static ro.axon.dot.EmployeeTestAttributes.CRT_USR;
import static ro.axon.dot.EmployeeTestAttributes.EMAIL;
import static ro.axon.dot.EmployeeTestAttributes.FIRST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.LAST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.MDF_TMS;
import static ro.axon.dot.EmployeeTestAttributes.MDF_USR;
import static ro.axon.dot.EmployeeTestAttributes.ROLE;
import static ro.axon.dot.EmployeeTestAttributes.STATUS;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;
import static ro.axon.dot.EmployeeTestAttributes.V;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsListItem;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.model.VacationDaysModifyDetails;
import ro.axon.dot.service.EmpYearlyDaysOffService;

@ExtendWith(MockitoExtension.class)
class EmployeeYearlyDaysOffApiTest {
  public static final TeamDetailsListItem teamDetails1 = new TeamDetailsListItem();
  public static final EmployeeDetailsListItem employee1 = new EmployeeDetailsListItem();
  public static final EmpYearlyDaysOffDetailsListItem yearlyDaysOff = new EmpYearlyDaysOffDetailsListItem();


  @Mock
  EmpYearlyDaysOffService empYearlyDaysOffService;

  @InjectMocks
  EmpYearlyDaysOffApi empYearlyDaysOffApi;

  MockMvc mockMvc;

  @BeforeEach
  void setUp(){
    mockMvc = MockMvcBuilders.standaloneSetup(empYearlyDaysOffApi)
        .setControllerAdvice(new ApiExceptionHandler())
            .build();

    teamDetails1.setName("AxonTeam");

    employee1.setFirstName(FIRST_NAME);
    employee1.setLastName(LAST_NAME);
    employee1.setEmail(EMAIL);
    employee1.setCrtUsr(CRT_USR);
    employee1.setCrtTms(CRT_TMS);
    employee1.setMdfUsr(MDF_USR);
    employee1.setMdfTms(MDF_TMS);
    employee1.setRole(ROLE);
    employee1.setStatus(STATUS);
    employee1.setV(V);
    employee1.setUsername(USERNAME);
    employee1.setTeamDetails(teamDetails1);
    employee1.setTotalVacationDays(21);


    yearlyDaysOff.setYear(2023);
    yearlyDaysOff.setId(1L);
    yearlyDaysOff.setTotalNoDays(12);
    yearlyDaysOff.setEmployeeId(employee1.getId());


  }

  @Test
  void changeVacationDays() throws Exception
  {
    VacationDaysModifyDetails v = new VacationDaysModifyDetails();
    v.setDescription("desc");
    v.setNoDays(2);

    List<String> ids = new ArrayList<>();
    ids.add( employee1.getId() );
    v.setEmployeeIds(ids);

    v.setType(VacationDaysChangeTypeEnum.INCREASE);

    ObjectMapper objectMapper = new ObjectMapper();

    mockMvc.perform(post("/api/v1/employees/days-off")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(v))
        )
        .andExpect(status().is(204));
  }
}