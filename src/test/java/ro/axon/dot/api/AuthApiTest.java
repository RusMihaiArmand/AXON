package ro.axon.dot.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.SignedJWT;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.RefreshTokenEty;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TokenStatus;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.model.RefreshTokenRequest;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.security.TokenUtilSetup;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthApiTest {
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private EmployeeService employeeService;
  @Mock
  private RefreshTokenService refreshTokenService;

  private JwtTokenUtil tokenUtil;
  AuthApi api;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    tokenUtil = new TokenUtilSetup().getTokenUtil();

    api = new AuthApi(passwordEncoder, tokenUtil, employeeService, refreshTokenService);
    mockMvc = MockMvcBuilders.standaloneSetup(api).build();
  }

  @Test
  void login() {

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

    LoginRequest loginRequest = new LoginRequest(employee.getUsername(), "axon_"+employee.getUsername());

    when(employeeService.loadEmployeeByUsername(employee.getUsername())).thenReturn(employee);
    when(passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())).thenReturn(true);

    ResponseEntity<?> responseEntity = api.login(loginRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertNotNull(responseEntity.getBody());

    LoginResponse response = (LoginResponse) responseEntity.getBody();
    assertTrue(response.getAccessTokenExpirationTime().isAfter(LocalDateTime.now()));
    assertTrue(response.getRefreshTokenExpirationTime().isAfter(LocalDateTime.now()));
  }

  @Test
  void login_user_not_found(){
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

    LoginRequest loginRequest = new LoginRequest(employee.getUsername(), "axon_"+employee.getUsername());

    when(employeeService.loadEmployeeByUsername(employee.getUsername())).thenThrow(new BusinessException(
        BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    BusinessException exception = assertThrows(BusinessException.class, () -> api.login(loginRequest));

    assertEquals(BusinessErrorCode.EMPLOYEE_NOT_FOUND, exception.getError().getErrorDescription());
  }

  @Test
  void login_passwords_dont_match(){
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

    LoginRequest loginRequest = new LoginRequest(employee.getUsername(), "axon_"+employee.getUsername());

    when(passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())).thenReturn(false);
    when(employeeService.loadEmployeeByUsername(employee.getUsername())).thenReturn(employee);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.login(loginRequest));

    assertEquals(BusinessErrorCode.PASSWORD_NOT_MATCHING, exception.getError().getErrorDescription());
  }

  @Test
  void refresh() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenReturn(refreshTokenEty);

    ResponseEntity<?> responseEntity = api.refresh(tokenRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertNotNull(responseEntity.getBody());

    LoginResponse response = (LoginResponse) responseEntity.getBody();
    assertNotEquals(response.getRefreshToken(), refreshToken);
  }

  @Test
  void refresh_token_not_found() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenThrow(new BusinessException(BusinessExceptionElement
        .builder()
        .errorDescription(BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND)
        .build()));

    BusinessException exception = assertThrows(BusinessException.class, () -> api.refresh(tokenRequest));

    assertEquals(BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getError().getErrorDescription());
  }
  @Test
  void refresh_token_revoked() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.REVOKED);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.refresh(
        tokenRequest));

    assertEquals(BusinessErrorCode.TOKEN_REVOKED, exception.getError().getErrorDescription());
  }

  @Test
  void refresh_token_expired() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now.minusMinutes(1000).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.refresh(
        tokenRequest));

    assertEquals(BusinessErrorCode.TOKEN_EXPIRED, exception.getError().getErrorDescription());

  }

  @Test
  void refresh_token_audience_error() {

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

    EmployeeEty employee2 = new EmployeeEty(
        "222",
        "alex",
        "smith",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "alex2",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee2);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now.minusMinutes(30).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.refresh(
        tokenRequest));

    assertEquals(BusinessErrorCode.AUDIENCE_DOES_NOT_MATCH, exception.getError().getErrorDescription());

  }
  @Test
  void logout() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(any())).thenReturn(refreshTokenEty);

    ResponseEntity<?> responseEntity = api.logout(tokenRequest);

    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
  }

  @Test
  void logout_audience_error() {

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

    EmployeeEty employee2 = new EmployeeEty(
        "222",
        "alex",
        "smith",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "alex2",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee2);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(any())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.logout(
        tokenRequest));
    assertEquals(BusinessErrorCode.AUDIENCE_DOES_NOT_MATCH, exception.getError().getErrorDescription());
  }

  @Test
  void logout_token_expired() {

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
    LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now.minusMinutes(1000).toInstant(ZoneOffset.UTC));

    when(refreshTokenService.findTokenByKeyId(any())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.logout(
        tokenRequest));
    assertEquals(BusinessErrorCode.TOKEN_EXPIRED, exception.getError().getErrorDescription());
  }
}