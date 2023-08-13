package ro.axon.dot.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private EmployeeService employeeService;
  @Mock
  private RefreshTokenService refreshTokenService;
  @Mock
  private FilterChain chain;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  private final JwtTokenUtil tokenUtil;
  private JwtRequestFilter filter;
  private final LocalDateTime now;

  public JwtRequestFilterTest() {
    TokenUtilSetup tokenUtilSetup = new TokenUtilSetup();
    tokenUtil = tokenUtilSetup.getTokenUtil();
    now = tokenUtilSetup.getNow();
  }

  @BeforeEach
  public void setUp() throws IOException {
    filter = new JwtRequestFilter(refreshTokenService, employeeService, tokenUtil);
  }

  @Test
  void doFilterInternal_CorrectHeader() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    SignedJWT token = tokenUtil.generateAccessToken(employee, now);

    when(employeeService.loadEmployeeByUsername(any())).thenReturn(employee);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token.serialize());

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

  }

  @Test
  void doFilterInternal_BadHeader() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    SignedJWT token = tokenUtil.generateAccessToken(employee, now);

    when(request.getHeader("Authorization")).thenReturn(token.serialize());

    assertThrows(BusinessException.class, () -> filter.doFilterInternal(request, response, chain));

  }

  @Test
  void doFilterInternal_InvalidUsername() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    employee.setUsername("");
    SignedJWT token = tokenUtil.generateAccessToken(employee, now);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token.serialize());

    BusinessException e = assertThrows(BusinessException.class, () -> filter.doFilterInternal(request, response, chain));

    assertEquals(BusinessErrorCode.TOKEN_HAS_NO_USERNAME, e.getError().getErrorDescription());
  }

  @Test
  void doFilterInternal_Token_Expired() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    employee.setUsername("");
    SignedJWT token = tokenUtil.generateAccessToken(employee, now.minusMinutes(1000));

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token.serialize());

    BusinessException e = assertThrows(BusinessException.class, () -> filter.doFilterInternal(request, response, chain));

    assertEquals(BusinessErrorCode.TOKEN_EXPIRED, e.getError().getErrorDescription());
  }
}