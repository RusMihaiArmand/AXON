package ro.axon.dot.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final OffsetDateTime accessTokenExpirationTime;
  private final OffsetDateTime refreshTokenExpirationTime;

}