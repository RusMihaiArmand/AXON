package ro.axon.dot.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;

@Component
@EnableConfigurationProperties(JwtTokenUtilProperties.class)
public class JwtTokenUtil {

	private final JwtTokenUtilProperties properties;

	private RSAPublicKey publicKey;
	private RSAPrivateKey privateKey;

	public JwtTokenUtil(JwtTokenUtilProperties properties, ResourceLoader resourceLoader) {
		this.properties = properties;

		readKeys(resourceLoader);
	}

	private void readKeys(ResourceLoader resourceLoader){
		Resource publicKeyResource = resourceLoader.getResource(properties.getPublicKeyLocation());
		Resource privateKeyResource = resourceLoader.getResource(properties.getPrivateKeyLocation());

		String publicKeyString;
		String privateKeyString;
		try {
			publicKeyString = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()));
			privateKeyString = new String(Files.readAllBytes(privateKeyResource.getFile().toPath()));
		} catch (IOException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.FAILED_TO_READ_KEYS)
					.build());
		}
		this.publicKey = RsaKeyConverters.x509().convert(new ByteArrayInputStream(publicKeyString.getBytes()));
		this.privateKey = RsaKeyConverters.pkcs8().convert(new ByteArrayInputStream(privateKeyString.getBytes()));
	}

	public SignedJWT generateAccessToken(EmployeeEty employee, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(publicKey).keyID(properties.getKeyId()).privateKey(privateKey).build();

		return setupToken(employee, jwk, "access", properties.getAccessTokenDuration(), currentTime);
	}

	public SignedJWT generateRefreshToken(EmployeeEty employee, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(publicKey).keyID(String.valueOf(UUID.randomUUID())).privateKey(privateKey).build();

		return setupToken(employee, jwk, "refresh", properties.getRefreshTokenDuration(), currentTime);
	}

	public SignedJWT regenerateRefreshToken(EmployeeEty employee, SignedJWT token, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(publicKey).keyID(token.getHeader().getKeyID()).privateKey(privateKey).build();

		return setupToken(employee, jwk, "refresh", properties.getRefreshTokenDuration(), currentTime);
	}

	private SignedJWT setupToken(EmployeeEty employee, JWK jwk, String tokenType, Long tokenDuration, LocalDateTime currentTime)
			throws BusinessException {
		RSAKey rsaJWK = new RSAKey.Builder(jwk.toRSAKey()).algorithm(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();

		final LocalDateTime expDate = currentTime.plusMinutes(tokenDuration);

		SignedJWT token = new SignedJWT(
				new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
				new JWTClaimsSet.Builder()
						.subject(String.valueOf(employee.getId()))
						.issuer(properties.getDomain())
						.audience(String.valueOf(employee.getId()))
						.claim("username", employee.getUsername())
						.claim("email", employee.getEmail())
						.notBeforeTime(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
						.claim("typ", tokenType)
						.expirationTime(Date.from(expDate.atZone(ZoneId.systemDefault()).toInstant())).build()
		);

		return signToken(token, rsaJWK);
	}

	private SignedJWT signToken(SignedJWT token, RSAKey rsaJWK) throws BusinessException {

		JWSSigner signer;

		try {
			signer = new RSASSASigner(rsaJWK);
		} catch (JOSEException e) {
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.SIGNER_CREATION_FAILED)
					.contextVariables(variables).build());
		}

		try {
			token.sign(signer);
			return token;
		} catch (JOSEException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_CANNOT_BE_SIGNED)
					.build());
		}
	}

	public JWTClaimsSet getClaimSet(SignedJWT token) throws BusinessException {
		try {
      return token.getJWTClaimsSet();
		} catch (ParseException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.CLAIMSET_NOT_AVAILABLE)
					.build());
		}
	}

	public void validateClaimSet(SignedJWT token) {
		DefaultJWTClaimsVerifier<?> verifier = new DefaultJWTClaimsVerifier<>(
				new JWTClaimsSet.Builder()
						.issuer(properties.getDomain())
						.build(),
				new HashSet<>(Arrays.asList("sub","aud", "nbf", "typ", "exp", "email", "username")));
		try {
			verifier.verify(getClaimSet(token), null);
		} catch (BadJWTException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.CLAIMSET_INVALID)
					.build());
		}
	}

	public SignedJWT parseToken(String token) throws BusinessException {
		try {
			return SignedJWT.parse(token);
		} catch (ParseException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_PARSING_FAILED)
					.build());
		}
	}

	public String getAudienceFromToken(SignedJWT token) {
		return getClaimSet(token).getAudience().get(0);
	}

	public String getUsernameFromToken(SignedJWT token) {

		String username = (String) getClaimSet(token).getClaim("username");

		if(username == null || username.isBlank()){
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_HAS_NO_USERNAME)
					.build());
		}
		else
			return username;
	}

	public Date getExpirationDateFromToken(SignedJWT token) {
		return getClaimSet(token).getExpirationTime();
	}

	public void isTokenExpired(SignedJWT token) {
		if(getClaimSet(token).getExpirationTime().before(new Date())){
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_EXPIRED)
					.build());
		}
	}

	public void validateToken(SignedJWT token, EmployeeEty employee) {
		final String username = getUsernameFromToken(token);

		if(!username.equals(employee.getUsername())) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_INVALID)
					.build());
		}
	}

	public boolean verifyToken(SignedJWT token) throws BusinessException {
		try {
			return token.verify(new RSASSAVerifier(publicKey));
		} catch (JOSEException e) {
			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.TOKEN_CANNOT_BE_VERIFIED)
					.build());
		}
	}
}
