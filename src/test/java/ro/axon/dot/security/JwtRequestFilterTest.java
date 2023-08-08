package ro.axon.dot.security;

import static org.mockito.ArgumentMatchers.any;
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

import com.nimbusds.jwt.SignedJWT;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.converter.RsaKeyConverters;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.service.EmployeeService;

class JwtRequestFilterTest {

  @Mock
  private EmployeeService employeeService;
  @Mock
  private FilterChain chain;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  private JwtTokenUtil tokenUtil;
  private JwtRequestFilter filter;

  private EmployeeEty employee;

  private LocalDateTime now;

  @BeforeEach
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);

    Path publicKey_path = Paths.get("config/jwk-public.pem");
    Path privateKey_path = Paths.get("config/jwk-private.pem");

    String publicKeyString = new String(Files.readAllBytes(publicKey_path));

    String privateKeyString = new String(Files.readAllBytes(privateKey_path));

    RSAPublicKey publicKey = RsaKeyConverters.x509().convert(new ByteArrayInputStream(publicKeyString.getBytes()));
    RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(new ByteArrayInputStream(privateKeyString.getBytes()));

    tokenUtil = new JwtTokenUtil(
        "AXON",
        100000L,
        200000L,
        "DOMAIN",
        publicKey,
        privateKey
    );
    filter = new JwtRequestFilter(employeeService, tokenUtil);

    now = LocalDateTime.now();

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);

    employee = setupEmployee();
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


  @Test
  void doFilterInternal_CorrectHeader() throws Exception {
    SignedJWT token = tokenUtil.generateAccessToken(employee, now);

    when(employeeService.loadEmployeeByUsername(any())).thenReturn(employee);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token.serialize());

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);


  }

  @Test
  void doFilterInternal_BadHeader() throws Exception {
    SignedJWT token = tokenUtil.generateAccessToken(employee, now);

    when(employeeService.loadEmployeeByUsername(any())).thenReturn(employee);
    when(request.getHeader("Authorization")).thenReturn(token.serialize());

    filter.doFilterInternal(request, response, chain);

    verify(response).sendError(401, "Bad token format");
  }
}