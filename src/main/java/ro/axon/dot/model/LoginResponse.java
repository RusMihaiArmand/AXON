package ro.axon.dot.model;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;
  private final String refreshToken;
  private final LocalDateTime accessTokenExpirationTime;
  private final LocalDateTime refreshTokenExpirationTime;

  @Override
  public String toString() {
    return "LoginResponse{" +
        "accessToken='" + accessToken + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        ", accessTokenExpirationTime=" + accessTokenExpirationTime +
        ", refreshTokenExpirationTime=" + refreshTokenExpirationTime +
        '}';
  }
}