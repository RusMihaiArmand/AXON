package ro.axon.dot.security;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateConverter;
import org.springframework.security.converter.RsaKeyConverters;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.exceptions.BusinessException;

class JwtTokenUtilTest {

  private final JwtTokenUtilProperties properties;

  private final JwtTokenUtil tokenUtil;
  private EmployeeEty employee;

  private final LocalDateTime now;

  public JwtTokenUtilTest() {
    TokenUtilSetup tokenUtilSetup = new TokenUtilSetup();

    properties = tokenUtilSetup.getProperties();
    tokenUtil = tokenUtilSetup.getTokenUtil();
    now = tokenUtilSetup.getNow();

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
    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    JWTClaimsSet claimsSet = accessToken.getJWTClaimsSet();

    assertEquals(String.valueOf(employee.getId()), claimsSet.getSubject());
    assertEquals(properties.domain(), claimsSet.getIssuer());
    assertEquals(employee.getId(), claimsSet.getAudience().get(0));
    assertEquals(employee.getUsername(), claimsSet.getClaim("username"));
    assertEquals(employee.getEmail(), claimsSet.getClaim("email"));
    assertEquals("access", claimsSet.getClaim("typ"));
    assertTrue(claimsSet.getExpirationTime().after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())));
  }

  @Test
  void generateRefreshToken() throws ParseException, BusinessException {
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
    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertEquals(employee.getUsername(), tokenUtil.getUsernameFromToken(accessToken));
  }

  @Test
  void getExpirationDateFromToken() throws BusinessException, ParseException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertEquals(accessToken.getJWTClaimsSet().getExpirationTime().toInstant(), tokenUtil.getExpirationDateFromToken(accessToken).toInstant(ZoneOffset.UTC));
  }

  @Test
  void isTokenExpired() throws  BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);
    assertDoesNotThrow(() -> tokenUtil.isTokenExpired(tokenUtil.getExpirationDateFromToken(accessToken)));

    SignedJWT token2 = tokenUtil.generateAccessToken(employee, now.minusHours(1));

    assertNotNull(token2);
    assertThrows(BusinessException.class, () -> tokenUtil.isTokenExpired(tokenUtil.getExpirationDateFromToken(token2)));
  }

  @Test
  void validateToken() {
    SignedJWT accessToken;
    try {
      accessToken = tokenUtil.generateAccessToken(employee, now);
    } catch (BusinessException e) {
      throw new RuntimeException(e);
    }

    EmployeeEty test = new EmployeeEty(
        "22",
        "jon",
        "doe",
        "email@bla.com",
        "",
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
        "",
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
        "user",
        "active",
        LocalDate.now(),
        LocalDate.now(),
        "user12345",
        "pass",
        new TeamEty(),
        null
    );

    assertNotNull(accessToken);
    assertDoesNotThrow(() -> tokenUtil.validateToken(accessToken, employee));

    assertThrows(BusinessException.class, () -> tokenUtil.validateToken(accessToken, test));
  }

  @Test
  void verifyToken() throws BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(employee, now);

    assertNotNull(accessToken);

    assertDoesNotThrow(() -> tokenUtil.verifyToken(accessToken));
  }
}