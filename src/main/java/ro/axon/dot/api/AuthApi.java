package ro.axon.dot.api;

import com.nimbusds.jwt.SignedJWT;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.RefreshTokenEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.model.TokenRequest;
import ro.axon.dot.model.TeamDetails;
import ro.axon.dot.model.UserDetailsResponse;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.domain.TokenStatus;
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
  public ResponseEntity<?> refresh(@RequestBody @Valid TokenRequest request) {

    Pair<SignedJWT, RefreshTokenEty> tokenEtyPair = parseAndCheckToken(request.getRefreshToken());

    SignedJWT refreshToken = tokenEtyPair.getFirst();
    RefreshTokenEty fromDB = tokenEtyPair.getSecond();

    isTokenRevoked(fromDB);

    final LocalDateTime now = LocalDateTime.now();
    refreshToken = regenerateToken(refreshToken, fromDB, now);

    SignedJWT accessToken = jwtTokenUtil.generateAccessToken(fromDB.getEmployee(), now);

    return ResponseEntity.ok(new LoginResponse(
        accessToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(accessToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<?> logout(@RequestBody @Valid TokenRequest request) {

    Pair<SignedJWT, RefreshTokenEty> tokenEtyPair = parseAndCheckToken(request.getRefreshToken());

    RefreshTokenEty fromDB = tokenEtyPair.getSecond();
    fromDB.setMdfTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    fromDB.setStatus(TokenStatus.REVOKED);

    refreshTokenService.saveRefreshToken(fromDB);

    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/user")
  public ResponseEntity<?> getUserDetails(@RequestBody @Valid TokenRequest request) {

    SignedJWT token = jwtTokenUtil.parseToken(request.getRefreshToken());

    EmployeeEty employee = employeeService.loadEmployeeByUsername(
        jwtTokenUtil.getUsernameFromToken(token));
    
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

  private Pair<SignedJWT, RefreshTokenEty> parseAndCheckToken(String tokenString){

    SignedJWT token = jwtTokenUtil.parseToken(tokenString);
    RefreshTokenEty tokenEty = refreshTokenService.findTokenByKeyId(token.getHeader().getKeyID());

    checkAudience(token, tokenEty);
    isTokenExpired(tokenEty);

    return Pair.of(token, tokenEty);
  }

  private void isTokenExpired(RefreshTokenEty refreshTokenEty){
    jwtTokenUtil.isTokenExpired(refreshTokenEty.getExpTms().atOffset(ZoneOffset.UTC).toLocalDateTime());
  }

  private void isTokenRevoked(RefreshTokenEty refreshTokenEty){
    if(refreshTokenEty.getStatus().equals(TokenStatus.REVOKED)){
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

    tokenEty.setMdfTms(now.toInstant(ZoneOffset.UTC));
    tokenEty.setExpTms(jwtTokenUtil.getExpirationDateFromToken(toReturn).toInstant(ZoneOffset.UTC));
    refreshTokenService.saveRefreshToken(tokenEty);

    return toReturn;
  }

  private void createRefreshToken(SignedJWT refreshToken, EmployeeEty employee, LocalDateTime now) {

    RefreshTokenEty refreshTokenEty = new RefreshTokenEty(refreshToken.getHeader().getKeyID(),
        TokenStatus.ACTIVE,
        employee,
        now.toInstant(ZoneOffset.UTC),
        now.toInstant(ZoneOffset.UTC),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken).toInstant(ZoneOffset.UTC));

    refreshTokenService.saveRefreshToken(refreshTokenEty);
  }

}
