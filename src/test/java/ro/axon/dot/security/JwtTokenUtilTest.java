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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateConverter;
import org.springframework.security.converter.RsaKeyConverters;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.exceptions.BusinessException;

class JwtTokenUtilTest {

  public static final String KEY_ID = "AXON";
  public static final Long ACCESS_TOKEN_DURATION = 60000L;
  public static final Long REFRESH_TOKEN_DURATION = 120000L;
  public static final String DOMAIN = "myDomain";
  private final JwtTokenUtil tokenUtil;
  private final EmployeeEty userDetails;

  private final LocalDateTime now = LocalDateTime.now();

  public JwtTokenUtilTest() throws IOException {

    Path publicKey_path = Paths.get("config/jwk-public.pem");
    Path privateKey_path = Paths.get("config/jwk-private.pem");

    String publicKeyString = new String(Files.readAllBytes(publicKey_path));

    String privateKeyString = new String(Files.readAllBytes(privateKey_path));

    RSAPublicKey publicKey = RsaKeyConverters.x509().convert(new ByteArrayInputStream(publicKeyString.getBytes()));
    RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(new ByteArrayInputStream(privateKeyString.getBytes()));

    tokenUtil = new JwtTokenUtil(
        KEY_ID,
        ACCESS_TOKEN_DURATION,
        REFRESH_TOKEN_DURATION,
        DOMAIN,
        publicKey,
        privateKey
    );
    userDetails = setupEmployee();
    userDetails.setPassword("$2a$10$xD/Wy1H0efN3mYqlT4vqT.bvCmw4nviJ.Ji.wfymIJKjm0JZ/YKmS");
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
  void generateAccessToken() throws ParseException, BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(userDetails, now);

    assertNotNull(accessToken);
    JWTClaimsSet claimsSet = accessToken.getJWTClaimsSet();

    assertEquals(String.valueOf(userDetails.getId()), claimsSet.getSubject());
    assertEquals(DOMAIN, claimsSet.getIssuer());
    assertEquals(userDetails.getId(), claimsSet.getAudience().get(0));
    assertEquals(userDetails.getUsername(), claimsSet.getClaim("username"));
    assertEquals(userDetails.getEmail(), claimsSet.getClaim("email"));
    assertEquals("access", claimsSet.getClaim("typ"));
    assertTrue(claimsSet.getExpirationTime().after(Date.from(now.toInstant(ZoneOffset.UTC))));
  }

  @Test
  void generateRefreshToken() throws ParseException, BusinessException {
    SignedJWT refreshToken = tokenUtil.generateRefreshToken(userDetails, now);

    assertNotNull(refreshToken);
    JWTClaimsSet claimsSet = refreshToken.getJWTClaimsSet();

    assertEquals(String.valueOf(userDetails.getId()), claimsSet.getSubject());
    assertEquals(DOMAIN, claimsSet.getIssuer());
    assertEquals(userDetails.getId(), claimsSet.getAudience().get(0));
    assertEquals(userDetails.getUsername(), claimsSet.getClaim("username"));
    assertEquals(userDetails.getEmail(), claimsSet.getClaim("email"));
    assertEquals("refresh", claimsSet.getClaim("typ"));
    assertTrue(claimsSet.getExpirationTime().after(Date.from(now.toInstant(ZoneOffset.UTC))));
  }

  @Test
  void getUsernameFromToken() throws BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(userDetails, now);

    assertNotNull(accessToken);
    assertEquals(userDetails.getUsername(), tokenUtil.getUsernameFromToken(accessToken));
  }

  @Test
  void getExpirationDateFromToken() throws BusinessException, ParseException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(userDetails, now);

    assertNotNull(accessToken);
    assertEquals(accessToken.getJWTClaimsSet().getExpirationTime(), tokenUtil.getExpirationDateFromToken(accessToken));
  }

  @Test
  void isTokenExpired() throws  BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(userDetails, now);

    assertNotNull(accessToken);
    assertFalse(tokenUtil.isTokenExpired(accessToken));

  }

  @Test
  void validateToken() {
    SignedJWT accessToken;
    try {
      accessToken = tokenUtil.generateAccessToken(userDetails, now);
    } catch (BusinessException e) {
      throw new RuntimeException(e);
    }

    EmployeeEty test = new EmployeeEty(
        "22",
        "jon",
        "doe",
        "email@bla.com",
        "",
        new Date().toInstant(),
        "",
        new Date().toInstant(),
        "user",
        "active",
        new LocalDateConverter().convertToEntityAttribute(new Date()),
        new LocalDateConverter().convertToEntityAttribute(new Date()),
        "user12345",
        "pass",
        new TeamEty(),
        null
    );

    assertNotNull(accessToken);
    assertDoesNotThrow(() -> tokenUtil.validateToken(accessToken, userDetails));

    BusinessException exception = assertThrows(BusinessException.class, () -> tokenUtil.validateToken(accessToken, test));
    assertTrue(exception.getMessage().contains("Token invalid"));
  }

  @Test
  void verifyToken() throws BusinessException {
    SignedJWT accessToken = tokenUtil.generateAccessToken(userDetails, now);

    assertNotNull(accessToken);

    assertTrue(tokenUtil.verifyToken(accessToken));
  }
}