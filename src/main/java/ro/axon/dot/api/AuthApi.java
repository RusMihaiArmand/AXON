package ro.axon.dot.api;

import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.config.component.JwtTokenUtil;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.domain.entity.RefreshTokenEty;
import ro.axon.dot.domain.enums.TokenStatus;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.model.RefreshTokenRequest;
import ro.axon.dot.model.TeamDetails;
import ro.axon.dot.model.UserDetailsResponse;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.RefreshTokenService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthApi {

  private final PasswordEncoder passwordEncoder;
  private final JwtTokenUtil jwtTokenUtil;
  private final EmployeeService employeeService;
  private final RefreshTokenService refreshTokenService;
  private final Clock clock;

  @PostMapping(value = "/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {

    EmployeeEty employee = employeeService.loadEmployeeByUsername(request.getUsername());
    verifyPassword(request.getPassword(), employee);
    Instant now = clock.instant();

    final SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);
    final SignedJWT refreshToken = jwtTokenUtil.generateRefreshToken(employee, now);
    createRefreshToken(refreshToken, employee, now);

    Instant accessTokenExpiration = jwtTokenUtil.getExpirationDateFromToken(accessToken);
    Instant refreshTokenExpiration = jwtTokenUtil.getExpirationDateFromToken(refreshToken);

    return ResponseEntity.ok(LoginResponse
        .builder()
        .accessToken(accessToken.serialize())
        .refreshToken(refreshToken.serialize())
        .accessTokenExpirationTime(
            OffsetDateTime.ofInstant(accessTokenExpiration, ZoneOffset.UTC).toLocalDateTime())
        .refreshTokenExpirationTime(
            OffsetDateTime.ofInstant(refreshTokenExpiration, ZoneOffset.UTC).toLocalDateTime())
        .build());
  }

  @PostMapping(value = "/refresh")
  public ResponseEntity<?> refresh(@RequestBody @Valid RefreshTokenRequest request) {

    Pair<SignedJWT, RefreshTokenEty> tokenEtyPair = parseAndCheckToken(request.getRefreshToken());

    SignedJWT refreshToken = tokenEtyPair.getFirst();
    RefreshTokenEty fromDB = tokenEtyPair.getSecond();

    isTokenRevoked(fromDB);

    return regenerateToken(refreshToken, fromDB);
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<?> logout(@RequestBody @Valid RefreshTokenRequest request) {

    Pair<SignedJWT, RefreshTokenEty> tokenEtyPair = parseAndCheckToken(request.getRefreshToken());

    RefreshTokenEty fromDB = tokenEtyPair.getSecond();
    fromDB.setMdfTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    fromDB.setStatus(TokenStatus.REVOKED);

    refreshTokenService.saveRefreshToken(fromDB);

    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/user")
  public ResponseEntity<?> getUserDetails() {

    EmployeeEty employee = employeeService.loadEmployeeById(jwtTokenUtil.getLoggedUserId());

    return ResponseEntity.ok(
        UserDetailsResponse.builder()
            .employeeId(employee.getId())
            .username(employee.getUsername())
            .roles(List.of(employee.getRole()))
            .teamDetails(TeamDetails.builder()
                .teamId(employee.getTeam().getId())
                .name(employee.getTeam().getName())
                .build())
            .build());
  }

  private Pair<SignedJWT, RefreshTokenEty> parseAndCheckToken(String tokenString) {

    SignedJWT token = jwtTokenUtil.parseToken(tokenString);
    RefreshTokenEty tokenEty = refreshTokenService.findTokenByKeyId(token.getHeader().getKeyID());

    checkAudience(token, tokenEty);
    isTokenExpired(tokenEty);

    return Pair.of(token, tokenEty);
  }

  private void isTokenExpired(RefreshTokenEty refreshTokenEty) {

    jwtTokenUtil.isTokenExpired(
        refreshTokenEty.getExpTms().atOffset(ZoneOffset.UTC).toLocalDateTime());
  }

  private void isTokenRevoked(RefreshTokenEty refreshTokenEty) {
    if (refreshTokenEty.getStatus().equals(TokenStatus.REVOKED)) {
      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.TOKEN_REVOKED)
          .build());
    }
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

    if (!jwtTokenUtil.getAudienceFromToken(refreshToken)
        .equals(refreshTokenEty.getEmployee().getId())) {
      Map<String, Object> variables = new HashMap<>();
      variables.put("token", refreshToken.serialize());
      variables.put("username", refreshTokenEty.getEmployee().getUsername());

      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.AUDIENCE_DOES_NOT_MATCH)
          .contextVariables(variables).build());
    }
  }

  private ResponseEntity<?> regenerateToken(SignedJWT token, RefreshTokenEty tokenEty) {
    final Instant now = clock.instant();
    SignedJWT accessToken = jwtTokenUtil.generateAccessToken(tokenEty.getEmployee(), now);
    SignedJWT refreshToken = jwtTokenUtil.regenerateRefreshToken(tokenEty.getEmployee(), token, now);
    Instant accessTokenExpiration = jwtTokenUtil.getExpirationDateFromToken(accessToken);
    Instant refreshTokenExpiration = jwtTokenUtil.getExpirationDateFromToken(refreshToken);

    tokenEty.setMdfTms(now);
    tokenEty.setExpTms(refreshTokenExpiration);
    refreshTokenService.saveRefreshToken(tokenEty);

    return ResponseEntity.ok(LoginResponse
        .builder()
        .accessToken(accessToken.serialize())
        .refreshToken(refreshToken.serialize())
        .accessTokenExpirationTime(
            OffsetDateTime.ofInstant(accessTokenExpiration, ZoneOffset.UTC).toLocalDateTime())
        .refreshTokenExpirationTime(
            OffsetDateTime.ofInstant(refreshTokenExpiration, ZoneOffset.UTC).toLocalDateTime())
        .build());
  }

  private void createRefreshToken(SignedJWT refreshToken, EmployeeEty employee, Instant now) {
    RefreshTokenEty refreshTokenEty = new RefreshTokenEty(refreshToken.getHeader().getKeyID(),
        TokenStatus.ACTIVE,
        employee,
        now,
        now,
        jwtTokenUtil.getExpirationDateFromToken(refreshToken));

    refreshTokenService.saveRefreshToken(refreshTokenEty);
  }

}
