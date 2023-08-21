package ro.axon.dot.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final LocalDateTime accessTokenExpirationTime;
  private final LocalDateTime refreshTokenExpirationTime;

}