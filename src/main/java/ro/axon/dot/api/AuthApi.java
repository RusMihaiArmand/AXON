package ro.axon.dot.api;

import com.nimbusds.jwt.SignedJWT;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.RefreshTokenEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.model.RefreshRequest;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.domain.TokenStatus;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.RefreshTokenService;

@RestController
@RequestMapping("/api/v1")
public class AuthApi {

  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private EmployeeService employeeService;
  @Autowired
  private RefreshTokenService refreshTokenService;

  @PostMapping(value = "/register")
  public ResponseEntity<?> register(@RequestBody @Valid EmployeeDetailsListItem employee) {
    return ResponseEntity.ok(employeeService.createEmployee(employee));
  }

  @PostMapping(value = "/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {

    EmployeeEty employee = employeeService.loadEmployeeByUsername(loginRequest.getUsername());

    verifyPassword(loginRequest.getPassword(), employee);

    final LocalDateTime now = LocalDateTime.now();

    final SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);
    final SignedJWT refreshToken = jwtTokenUtil.generateRefreshToken(employee, now);

    saveRefreshToken(refreshToken, employee, now);

    return ResponseEntity.ok(new LoginResponse(
        accessToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(accessToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  @PostMapping(value = "/refresh")
  public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {

    final LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = jwtTokenUtil.parseToken(refreshRequest.getRefreshToken());

    RefreshTokenEty fromDB = refreshTokenService.findTokenByKeyId(
        refreshToken.getHeader().getKeyID());
    EmployeeEty employee = fromDB.getEmployee();

    checkAudience(refreshToken, employee);
    checkStatus(fromDB);
    checkIfExpired(fromDB);

    refreshToken = regenerateToken(refreshToken, fromDB, employee, now);

    SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);

    return ResponseEntity.ok(new LoginResponse(
        accessToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(accessToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  private void verifyPassword(String password, EmployeeEty employee) {
    if (!passwordEncoder.matches(password, employee.getPassword())) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("username", employee.getUsername());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.PASSWORD_NOT_MATCHING)
          .contextVariables(variables).build());
    }
  }

  private void checkAudience(SignedJWT refreshToken, EmployeeEty employee) {
    if (!jwtTokenUtil.getAudienceFromToken(refreshToken).equals(employee.getId())) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("token", refreshToken.serialize());
      variables.put("username", employee.getUsername());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.AUDIENCE_DOES_NOT_MATCH)
          .contextVariables(variables).build());
    }
  }

  private void checkStatus(RefreshTokenEty refreshToken) {
    if (!refreshToken.getStatus().equals(TokenStatus.ACTIVE)) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("token", refreshToken.getId());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.TOKEN_REVOKED)
          .contextVariables(variables).build());
    }
  }

  private void checkIfExpired(RefreshTokenEty token) {
    if (!token.getExpTms().isAfter(new Date().toInstant())) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("token", token.getId());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.TOKEN_EXPIRED)
          .contextVariables(variables).build());
    }
  }

  private SignedJWT regenerateToken(SignedJWT token, RefreshTokenEty tokenEty, EmployeeEty employee,
      LocalDateTime now) {
    tokenEty.setMdfTms(now.atZone(ZoneId.systemDefault()).toInstant());
    tokenEty.setExpTms(jwtTokenUtil.getExpirationDateFromToken(token).toInstant());
    refreshTokenService.saveRefreshToken(tokenEty);

    return jwtTokenUtil.regenerateRefreshToken(employee, token, now);
  }

  private void saveRefreshToken(SignedJWT refreshToken, EmployeeEty employee, LocalDateTime now) {

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty(refreshToken.getHeader().getKeyID(),
        TokenStatus.ACTIVE,
        employee,
        now.atZone(ZoneId.systemDefault()).toInstant(),
        now.atZone(ZoneId.systemDefault()).toInstant(),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken).toInstant());

    refreshTokenService.saveRefreshToken(refreshTokenEty);
  }

}
