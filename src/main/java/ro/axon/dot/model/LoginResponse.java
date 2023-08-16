package ro.axon.dot.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final LocalDateTime accessTokenExpirationTime;
  private final LocalDateTime refreshTokenExpirationTime;

}