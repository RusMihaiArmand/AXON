package ro.axon.dot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "ro.axon.core.token")
@ConstructorBinding
public record JwtTokenUtilProperties(
    String domain,
    String keyId,
    Long accessTokenDuration,
    Long refreshTokenDuration,
    String publicKeyLocation,
    String privateKeyLocation) {

}
