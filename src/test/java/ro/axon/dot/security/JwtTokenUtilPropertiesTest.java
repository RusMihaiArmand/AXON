package ro.axon.dot.security;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { JwtTokenUtilPropertiesTest.TestConfiguration.class })
class JwtTokenUtilPropertiesTest {

  @Autowired
  private JwtTokenUtilProperties properties;

  @Test
  @DisplayName("Should")
  void getValidProperties(){

    assertThat(properties.domain()).isEqualTo("https://localhost:8081/");
    assertThat(properties.keyId()).isEqualTo("AXON");
    assertThat(properties.accessTokenDuration()).isEqualTo(10L);
    assertThat(properties.refreshTokenDuration()).isEqualTo(20L);
    assertThat(properties.publicKeyLocation()).isEqualTo("file:config/jwk-public.pem");
    assertThat(properties.privateKeyLocation()).isEqualTo("file:config/jwk-private.pem");
  }

  @EnableConfigurationProperties(JwtTokenUtilProperties.class)
  public static class TestConfiguration {}

}