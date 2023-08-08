package ro.axon.dot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

import java.lang.reflect.Field;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.mapper.EmployeeMapperImpl;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthApiTest {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtTokenUtil jwtTokenUtil;
  @Mock
  private EmployeeService employeeService;
  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  AuthApi api;

  MockMvc mockMvc;

  private EmployeeEty employee;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(api).build();

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);

    employee = setupEmployee();
  }


  @Test
  void registerEmployee() throws Exception {
    ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();

    EmployeeDetailsListItem employeeDto = new EmployeeMapperImpl().mapEmployeeEtyToEmployeeDto(employee);

    when(employeeService.createEmployee(any())).thenReturn(employeeDto);

    mockMvc.perform(post("/api/v1/register")
            .content(toJSON(employeeDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].id").value(employee.getId()))
        .andExpect(jsonPath("$.items[0].firstName").value(employee.getFirstName()))
        .andExpect(jsonPath("$.items[0].username").value(employee.getUsername()))
        .andExpect(jsonPath("$.items[0].teamDetails.id").value(employee.getTeam().getId()))



    ;
  }

  @Test
  void createLoginToken() {
  }

  @Test
  void refresh() {
  }

  public static String toJSON(Object object)
      throws IllegalAccessException, JSONException {

    Class<?> c = object.getClass();
    JSONObject jsonObject = new JSONObject();

    for (Field field : c.getDeclaredFields()) {
      field.setAccessible(true);
      String name = field.getName();
      String value = String.valueOf(field.get(object));
      jsonObject.put(name, value);
    }

    System.out.println(jsonObject);
    return jsonObject.toString();
  }

  private static EmployeeEty setupEmployee() {
    EmployeeEty employee = new EmployeeEty();

    employee.setId(ID);
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
    return employee;
  }
}