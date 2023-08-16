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

  public TokenUtilSetup() {
    properties = new JwtTokenUtilProperties(
        "https://localhost:8081/",
        "TEST",
        10L,
        20L,
        "file:src/test/java/ro/axon/dot/security/keys/public_test_key.pem",
        "file:src/test/java/ro/axon/dot/security/keys/private_test_key.pem");

    tokenUtil = new JwtTokenUtil(properties, new DefaultResourceLoader());
  }
}
