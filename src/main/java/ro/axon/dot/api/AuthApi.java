package ro.axon.dot.api;

import com.nimbusds.jwt.SignedJWT;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {

    EmployeeEty employee = employeeService.loadEmployeeByUsername(request.getUsername());

    verifyPassword(request.getPassword(), employee);

    final LocalDateTime now = LocalDateTime.now();

    final SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);
    final SignedJWT refreshToken = jwtTokenUtil.generateRefreshToken(employee, now);

    createRefreshToken(refreshToken, employee, now);

    return ResponseEntity.ok(new LoginResponse(
        accessToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(accessToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  @PostMapping(value = "/refresh")
  public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest request) {

    final LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = jwtTokenUtil.parseToken(request.getRefreshToken());

    RefreshTokenEty fromDB = refreshTokenService.findTokenByKeyId(
        refreshToken.getHeader().getKeyID());

    checkAudience(refreshToken, fromDB);

    refreshToken = regenerateToken(refreshToken, fromDB, now);

    return ResponseEntity.ok(new LoginResponse(
        refreshToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<?> logout(@RequestBody @Valid RefreshRequest request) {

    final LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = jwtTokenUtil.parseToken(request.getRefreshToken());

    RefreshTokenEty fromDB = refreshTokenService.findTokenByKeyId(
        refreshToken.getHeader().getKeyID());

    checkAudience(refreshToken, fromDB);

    fromDB.setMdfTms(now.atZone(ZoneId.systemDefault()).toInstant());
    fromDB.setStatus(TokenStatus.REVOKED);

    refreshTokenService.saveRefreshToken(fromDB);

    return ResponseEntity.noContent().build();
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

  private void checkAudience(SignedJWT refreshToken, RefreshTokenEty refreshTokenEty) {
    if (!jwtTokenUtil.getAudienceFromToken(refreshToken).equals(refreshTokenEty.getEmployee().getId())) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("token", refreshToken.serialize());
      variables.put("username", refreshTokenEty.getEmployee().getUsername());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.AUDIENCE_DOES_NOT_MATCH)
          .contextVariables(variables).build());
    }
  }

  private SignedJWT regenerateToken(SignedJWT token, RefreshTokenEty tokenEty, LocalDateTime now) {

    SignedJWT toReturn = jwtTokenUtil.regenerateRefreshToken(tokenEty.getEmployee(), token, now);

    tokenEty.setMdfTms(now.atZone(ZoneId.systemDefault()).toInstant());
    tokenEty.setExpTms(jwtTokenUtil.getExpirationDateFromToken(toReturn).toInstant());
    refreshTokenService.saveRefreshToken(tokenEty);

    return toReturn;
  }

  private void createRefreshToken(SignedJWT refreshToken, EmployeeEty employee, LocalDateTime now) {

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty(refreshToken.getHeader().getKeyID(),
        TokenStatus.ACTIVE,
        employee,
        now.atZone(ZoneId.systemDefault()).toInstant(),
        now.atZone(ZoneId.systemDefault()).toInstant(),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken).toInstant());

    refreshTokenService.saveRefreshToken(refreshTokenEty);
  }

}
