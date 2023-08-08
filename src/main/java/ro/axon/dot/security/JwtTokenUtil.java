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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessExceptionElement;

@Component
public class JwtTokenUtil {

	private final String KEY_ID;
	private final Long ACCESS_TOKEN_DURATION;
	private final Long REFRESH_TOKEN_DURATION;
	private final String DOMAIN;
	private final RSAPublicKey PUBLIC_KEY;
	private final RSAPrivateKey PRIVATE_KEY;

	public JwtTokenUtil(
			@Value("${axon.app.keyID}") String KEY_ID,
			@Value("${axon.app.accessTokenDuration}") Long ACCESS_TOKEN_DURATION,
			@Value("${axon.app.refreshTokenDuration}") Long REFRESH_TOKEN_DURATION,
			@Value("${axon.app.domain}") String DOMAIN,
			@Value("${spring.security.oauth2.resource-server.jwt.public-key-location}") RSAPublicKey PUBLIC_KEY,
			@Value("${axon.app.private-key-location}") RSAPrivateKey PRIVATE_KEY) {

		this.KEY_ID = KEY_ID;
		this.ACCESS_TOKEN_DURATION = ACCESS_TOKEN_DURATION;
		this.REFRESH_TOKEN_DURATION = REFRESH_TOKEN_DURATION;
		this.DOMAIN = DOMAIN;
		this.PUBLIC_KEY = PUBLIC_KEY;
		this.PRIVATE_KEY = PRIVATE_KEY;
	}

	public SignedJWT generateAccessToken(EmployeeEty employee, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(PUBLIC_KEY).keyID(KEY_ID).privateKey(PRIVATE_KEY).build();

		return setupToken(employee, jwk, "access", ACCESS_TOKEN_DURATION, currentTime);
	}

	public SignedJWT generateRefreshToken(EmployeeEty employee, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(PUBLIC_KEY).keyID(String.valueOf(UUID.randomUUID())).privateKey(PRIVATE_KEY).build();

		return setupToken(employee, jwk, "refresh", REFRESH_TOKEN_DURATION, currentTime);
	}

	public SignedJWT regenerateRefreshToken(EmployeeEty employee, SignedJWT token, LocalDateTime currentTime)
			throws BusinessException {
		JWK jwk = new RSAKey.Builder(PUBLIC_KEY).keyID(token.getHeader().getKeyID()).privateKey(PRIVATE_KEY).build();

		return setupToken(employee, jwk, "refresh", REFRESH_TOKEN_DURATION, currentTime);
	}

	private SignedJWT setupToken(EmployeeEty employee, JWK jwk, String tokenType, Long tokenDuration, LocalDateTime currentTime)
			throws BusinessException {
		RSAKey rsaJWK = new RSAKey.Builder(jwk.toRSAKey()).algorithm(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();

		final LocalDateTime expDate = currentTime.plusMinutes(tokenDuration);

		SignedJWT token = new SignedJWT(
				new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
				new JWTClaimsSet.Builder()
						.subject(String.valueOf(employee.getId()))
						.issuer(DOMAIN)
						.audience(String.valueOf(employee.getId()))
						.claim("username", employee.getUsername())
						.claim("email", employee.getEmail())
						.notBeforeTime(Date.from(currentTime.toInstant(ZoneOffset.UTC)))
						.claim("typ", tokenType)
						.expirationTime(Date.from(expDate.toInstant(ZoneOffset.UTC))).build()
		);

		return signToken(token, rsaJWK);
	}

	private SignedJWT signToken(SignedJWT token, RSAKey rsaJWK) throws BusinessException {

		JWSSigner signer;

		try {
			signer = new RSASSASigner(rsaJWK);
		} catch (JOSEException e) {

			BusinessErrorCode errorCode = BusinessErrorCode.SIGNER_CREATION_FAILED;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(e, new BusinessExceptionElement(errorCode, variables));
		}

		try {
			token.sign(signer);
			return token;
		} catch (JOSEException e) {
			BusinessErrorCode errorCode = BusinessErrorCode.TOKEN_CANNOT_BE_SIGNED;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(e, new BusinessExceptionElement(errorCode, variables));
		}
	}

	public JWTClaimsSet getClaimSet(SignedJWT token) throws BusinessException {
		try {
			return token.getJWTClaimsSet();
		} catch (ParseException e) {
			BusinessErrorCode errorCode = BusinessErrorCode.CLAIMSET_NOT_AVAILABLE;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(e, new BusinessExceptionElement(errorCode, variables));
		}
	}

	public SignedJWT parseToken(String token) throws BusinessException {
		try {
			return SignedJWT.parse(token);
		} catch (ParseException e) {

			BusinessErrorCode errorCode = BusinessErrorCode.TOKEN_PARSING_FAILED;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(e, new BusinessExceptionElement(errorCode, variables));
		}
	}

	public String getAudienceFromToken(SignedJWT token) throws BusinessException {
		return getClaimSet(token).getAudience().get(0);
	}

	public String getUsernameFromToken(SignedJWT token) throws BusinessException {
		try {
			String username = (String) getClaimSet(token).getClaim("username");

			if(username == null){
				BusinessErrorCode errorCode = BusinessErrorCode.TOKEN_HAS_NO_USERNAME;
				Map<String, Object> variables = new HashMap<>();
				variables.put("token", token);

				throw new BusinessException(new RuntimeException(), new BusinessExceptionElement(errorCode, variables));
			}
			else
				return username;

		} catch (BusinessException e) {
			throw new BusinessException(e, e.getError());
		}
	}

	public Date getExpirationDateFromToken(SignedJWT token) throws BusinessException {
		return getClaimSet(token).getExpirationTime();
	}

	public Boolean isTokenExpired(SignedJWT token) throws BusinessException {
		return getClaimSet(token).getExpirationTime().before(new Date());
	}

	public void validateToken(SignedJWT token, EmployeeEty employee) throws BusinessException {
		final String username = getUsernameFromToken(token);
		if(!username.equals(employee.getUsername()) || isTokenExpired(token)) {
			BusinessErrorCode errorCode = BusinessErrorCode.TOKEN_INVALID;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);
			variables.put("employee", employee);

			throw new BusinessException(new RuntimeException(),
					new BusinessExceptionElement(errorCode, variables));
		}
	}

	public boolean verifyToken(SignedJWT token) throws BusinessException {
		try {
			return token.verify(new RSASSAVerifier(PUBLIC_KEY));
		} catch (JOSEException e) {
			BusinessErrorCode errorCode = BusinessErrorCode.TOKEN_CANNOT_BE_VERIFIED;
			Map<String, Object> variables = new HashMap<>();
			variables.put("token", token);

			throw new BusinessException(new RuntimeException(),
					new BusinessExceptionElement(errorCode, variables));
		}
	}
}
