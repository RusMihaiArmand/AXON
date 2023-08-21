package ro.axon.dot.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ro.axon.dot.config.component.JwtTokenUtil;
import ro.axon.dot.config.properties.JwtTokenUtilProperties;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.exceptions.BusinessException;

class JwtTokenUtilTest {

  private final JwtTokenUtilProperties properties;
  private final JwtTokenUtil tokenUtil;
  private EmployeeEty employee;
  private final Clock clock;

  public JwtTokenUtilTest() {
    TokenUtilSetup tokenUtilSetup = new TokenUtilSetup();

    properties = tokenUtilSetup.getProperties();
    tokenUtil = tokenUtilSetup.getTokenUtil();
    clock = Clock.systemDefaultZone();

    setupEmployee();
  }

  private void setupEmployee() {
    employee = new EmployeeEty();

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
    employee.setPassword("$2a$10$xD/Wy1H0efN3mYqlT4vqT.bvCmw4nviJ.Ji.wfymIJKjm0JZ/YKmS");
  }

  @Test
  void generateAccessToken() throws ParseException, BusinessException {
    final Instant now = clock.instant();

    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    JWTClaimsSet claimsSet = accessToken.getJWTClaimsSet();

    assertEquals(employee.getId(), claimsSet.getSubject());
    assertEquals(properties.domain(), claimsSet.getIssuer());
    assertEquals(employee.getId(), claimsSet.getAudience().get(0));
    assertEquals(employee.getUsername(), claimsSet.getClaim("username"));
    assertEquals(employee.getEmail(), claimsSet.getClaim("email"));
    assertEquals("access", claimsSet.getClaim("typ"));
    assertTrue(claimsSet.getExpirationTime().after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())));
  }

  @Test
  void generateRefreshToken() throws ParseException, BusinessException {
    final Instant now = clock.instant();

    SignedJWT refreshToken = tokenUtil.generateRefreshToken(employee, now);

    assertNotNull(refreshToken);
    JWTClaimsSet claimsSet = refreshToken.getJWTClaimsSet();

    assertEquals(String.valueOf(employee.getId()), claimsSet.getSubject());
    assertEquals(properties.domain(), claimsSet.getIssuer());
    assertEquals(employee.getId(), claimsSet.getAudience().get(0));
    assertEquals(employee.getUsername(), claimsSet.getClaim("username"));
    assertEquals(employee.getEmail(), claimsSet.getClaim("email"));
    assertEquals("refresh", claimsSet.getClaim("typ"));
    assertTrue(claimsSet.getExpirationTime().after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())));
  }

  @Test
  void getUsernameFromToken() throws BusinessException {
    final Instant now = clock.instant();

    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertEquals(employee.getUsername(), tokenUtil.getUsernameFromToken(accessToken));
  }

  @Test
  void getExpirationDateFromToken() throws BusinessException, ParseException {
    final Instant now = clock.instant();

    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertEquals(accessToken.getJWTClaimsSet().getExpirationTime().toInstant(), tokenUtil.getExpirationDateFromToken(accessToken));
  }

  @Test
  void isTokenExpired() throws  BusinessException {
    final Instant now = clock.instant();

    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertDoesNotThrow(() -> tokenUtil.isTokenExpired(tokenUtil.getExpirationDateFromToken(accessToken).atOffset(ZoneOffset.UTC).toLocalDateTime()));

    SignedJWT token2 = tokenUtil.generateAccessToken(employee, now.minusSeconds(60 * 60));

    assertNotNull(token2);
    assertThrows(BusinessException.class, () -> tokenUtil.isTokenExpired(tokenUtil.getExpirationDateFromToken(token2).atOffset(ZoneOffset.UTC).toLocalDateTime()));
  }

  @Test
  public void getLoggedUserId() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn("testUserId");

    Authentication authentication = new JwtAuthenticationToken(jwt);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String userId = tokenUtil.getLoggedUserId();

    assertEquals("testUserId", userId);
  }

}