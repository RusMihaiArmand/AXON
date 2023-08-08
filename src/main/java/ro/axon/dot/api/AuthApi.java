package ro.axon.dot.api;

import com.nimbusds.jwt.SignedJWT;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import ro.axon.dot.domain.RefreshTokenEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessExceptionElement;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.RefreshRequest;
import ro.axon.dot.security.TokenStatus;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.model.LoginRequest;
import ro.axon.dot.model.LoginResponse;
import ro.axon.dot.domain.EmployeeEty;
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
  public ResponseEntity<?> registerEmployee(@RequestBody @Valid EmployeeDetailsListItem employee) {
    try {
      return ResponseEntity.ok(employeeService.createEmployee(employee));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping(value = "/login")
  public ResponseEntity<?> createLoginToken(@RequestBody @Valid LoginRequest loginRequest) {

    EmployeeEty employee;

    //Verify employee exists
    employee = employeeService.loadEmployeeByUsername(loginRequest.getUsername());


    //Verify password
    verifyPassword(loginRequest, employee);

    //Generate tokens
    final LocalDateTime now = LocalDateTime.now();

    final SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);
    final SignedJWT refreshToken = jwtTokenUtil.generateRefreshToken(employee, now);

    //Create refresh token entity
    RefreshTokenEty  refreshTokenEty = new RefreshTokenEty(refreshToken.getHeader().getKeyID(),
          TokenStatus.ACTIVE,
          employee,
          now.toInstant(ZoneOffset.UTC),
          now.toInstant(ZoneOffset.UTC),
          jwtTokenUtil.getExpirationDateFromToken(refreshToken).toInstant());

    //Save refresh token to db
    refreshTokenService.saveRefreshToken(refreshTokenEty);

    //Good return
    return ResponseEntity.ok(new LoginResponse(
        accessToken.serialize(),
        refreshToken.serialize(),
        jwtTokenUtil.getExpirationDateFromToken(accessToken),
        jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
  }

  @PostMapping(value = "/refresh")
  public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {

    final LocalDateTime now = LocalDateTime.now();

    SignedJWT refreshToken = parseToken(refreshRequest.getRefreshToken());
    String keyId = refreshToken.getHeader().getKeyID();


    //checks if token exists
    RefreshTokenEty fromDB;
    try {
      fromDB = refreshTokenService.findTokenByKeyId(keyId);
    } catch (BusinessException e) {
      throw new BusinessException(e, e.getError());
    }

    EmployeeEty employee = fromDB.getEmployee();

    //if audience matches
    try {
      if(jwtTokenUtil.getAudienceFromToken(refreshToken).equals(employee.getId())){
        //if status is active
        if(fromDB.getStatus().equals(TokenStatus.ACTIVE)){
          //if is not expired
          if(fromDB.getExpTms().isAfter(new Date().toInstant())){

            refreshToken = jwtTokenUtil.regenerateRefreshToken(employee, refreshToken, now);
            fromDB.setMdfTms(now.toInstant(ZoneOffset.UTC));
            fromDB.setExpTms(jwtTokenUtil.getExpirationDateFromToken(refreshToken).toInstant());

            refreshTokenService.saveRefreshToken(fromDB);

            SignedJWT accessToken = jwtTokenUtil.generateAccessToken(employee, now);

            //Good return
            return ResponseEntity.ok(new LoginResponse(
                accessToken.serialize(),
                refreshToken.serialize(),
                jwtTokenUtil.getExpirationDateFromToken(accessToken),
                jwtTokenUtil.getExpirationDateFromToken(refreshToken)));
          }
          else
            return ResponseEntity.badRequest().body("Token expired");
        }
        else
          return ResponseEntity.badRequest().body("Token is revoked");
      }
      else
        return ResponseEntity.badRequest().body("Audience doesn't match");
    }
    catch (BusinessException e) {
      throw new BusinessException(e, e.getError());
    }

  }

  private SignedJWT parseToken(String token){
    return jwtTokenUtil.parseToken(token);
  }

  private void verifyPassword(LoginRequest loginRequest, EmployeeEty employee){
    if(!passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())){
      BusinessErrorCode errorCode = BusinessErrorCode.PASSWORD_NOT_MATCHING;
      Map<String, Object> variables = new HashMap<>();
      variables.put("username", employee.getUsername());

      throw new BusinessException(new RuntimeException(), new BusinessExceptionElement(errorCode, variables));
    }
  }
}
