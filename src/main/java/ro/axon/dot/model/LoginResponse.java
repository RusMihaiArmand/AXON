package ro.axon.dot.model;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final LocalDateTime accessTokenExpirationTime;
  private final LocalDateTime refreshTokenExpirationTime;

}