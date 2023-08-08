package ro.axon.dot.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "ro.axon.core.token")
@ConstructorBinding
@Getter
@Setter
public class JwtTokenUtilProperties {
  private String domain;
  private String keyId;

  private Long accessTokenDuration;
  private Long refreshTokenDuration;

  private String publicKeyLocation;
  private String privateKeyLocation;
}
