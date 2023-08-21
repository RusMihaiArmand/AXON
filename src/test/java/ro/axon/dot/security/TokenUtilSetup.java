package ro.axon.dot.security;

import java.time.Clock;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.DefaultResourceLoader;
import ro.axon.dot.config.component.JwtTokenUtil;
import ro.axon.dot.config.properties.JwtTokenUtilProperties;

@Getter
@Setter
public class TokenUtilSetup {

  private JwtTokenUtilProperties properties;
  private JwtTokenUtil tokenUtil;

  public TokenUtilSetup() {
    properties = new JwtTokenUtilProperties(
        "https://localhost:8081/",
        "TEST",
        10L,
        20L,
        "file:src/test/java/ro/axon/dot/security/keys/public_test_key.pem",
        "file:src/test/java/ro/axon/dot/security/keys/private_test_key.pem");

    tokenUtil = new JwtTokenUtil(properties, new DefaultResourceLoader(), Clock.systemDefaultZone());
  }
}
