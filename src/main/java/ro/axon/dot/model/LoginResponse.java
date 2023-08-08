package ro.axon.dot.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final Date accessTokenExpirationTime;
  private final Date refreshTokenExpirationTime;


}