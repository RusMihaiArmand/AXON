package ro.axon.dot.security;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.DefaultResourceLoader;

@Getter
@Setter
public class TokenUtilSetup {

  private JwtTokenUtilProperties properties;

  private JwtTokenUtil tokenUtil;

  private final LocalDateTime now = LocalDateTime.now();

  public TokenUtilSetup() {
    properties = new JwtTokenUtilProperties();
    properties.setDomain("https://localhost:8081/");
    properties.setKeyId("AXON");
    properties.setAccessTokenDuration(10L);
    properties.setRefreshTokenDuration(20L);
    properties.setPublicKeyLocation("file:config/jwk-public.pem");
    properties.setPrivateKeyLocation("file:config/jwk-private.pem");

    tokenUtil = new JwtTokenUtil(properties, new DefaultResourceLoader());
  }
}
