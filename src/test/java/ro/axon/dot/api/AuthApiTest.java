package ro.axon.dot.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.domain.entity.RefreshTokenEty;
import ro.axon.dot.domain.entity.TeamEty;
import ro.axon.dot.domain.enums.TokenStatus;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.model.RefreshTokenRequest;
import ro.axon.dot.config.component.JwtTokenUtil;
import ro.axon.dot.model.TeamDetails;
import ro.axon.dot.model.UserDetailsResponse;
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
  private Clock clock;
  AuthApi api;

  @BeforeEach
  void setUp() {
    tokenUtil = new TokenUtilSetup().getTokenUtil();
    clock = Clock.systemDefaultZone();

    api = new AuthApi(passwordEncoder, tokenUtil, employeeService, refreshTokenService,
        clock);
  }

  @Test
  void login() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
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
    assertTrue(response.getAccessTokenExpirationTime().toInstant().isAfter(clock.instant()));
    assertTrue(response.getRefreshTokenExpirationTime().toInstant().isAfter(clock.instant()));
  }

  @Test
  void login_user_not_found(){
    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken));

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken));

    when(refreshTokenService.findTokenByKeyId(refreshToken.getHeader().getKeyID())).thenThrow(new BusinessException(BusinessExceptionElement
        .builder()
        .errorDescription(BusinessErrorCode.INVALID_REFRESH_TOKEN)
        .build()));

    BusinessException exception = assertThrows(BusinessException.class, () -> api.refresh(tokenRequest));

    assertEquals(BusinessErrorCode.INVALID_REFRESH_TOKEN, exception.getError().getErrorDescription());
  }
  @Test
  void refresh_token_revoked() {

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.REVOKED);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken));

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now.minusSeconds(100 * 60));

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "alex2",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee2);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now);

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken));

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "alex2",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );

    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee2);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(tokenUtil.getExpirationDateFromToken(refreshToken));

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
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        new TeamEty(),
        new HashSet<>(),
        new HashSet<>()
    );
    Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    RefreshTokenRequest tokenRequest = new RefreshTokenRequest(refreshToken.serialize());

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty();
    refreshTokenEty.setId(refreshToken.getHeader().getKeyID());
    refreshTokenEty.setEmployee(employee);
    refreshTokenEty.setStatus(TokenStatus.ACTIVE);
    refreshTokenEty.setExpTms(now.minusSeconds(100 * 60));

    when(refreshTokenService.findTokenByKeyId(any())).thenReturn(refreshTokenEty);

    BusinessException exception = assertThrows(BusinessException.class, () -> api.logout(
        tokenRequest));
    assertEquals(BusinessErrorCode.TOKEN_EXPIRED, exception.getError().getErrorDescription());
  }

  @Test
  void getUserDetails() {
    tokenUtil = mock(JwtTokenUtil.class);
    api = new AuthApi(passwordEncoder, tokenUtil, employeeService, refreshTokenService, clock);

    TeamEty teamEty = new TeamEty();
    teamEty.setId(1L);
    teamEty.setName("Test");

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        teamEty,
        new HashSet<>(),
        new HashSet<>()
    );

    when(tokenUtil.getLoggedUserId()).thenReturn("11");
    when(employeeService.loadEmployeeById("11")).thenReturn(employee);

    ResponseEntity<?> response = api.getUserDetails();

    assertEquals(HttpStatus.OK, response.getStatusCode());

    UserDetailsResponse expectedResponse = UserDetailsResponse.builder()
        .employeeId("11")
        .username("jon121")
        .roles(List.of("role.user"))
        .teamDetails(new TeamDetails(teamEty.getId(), teamEty.getName()))
        .build();

    UserDetailsResponse actualResponse = (UserDetailsResponse) response.getBody();
    assertEquals(expectedResponse, actualResponse);

    verify(employeeService).loadEmployeeById("11");
  }
}