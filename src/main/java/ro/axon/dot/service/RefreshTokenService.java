package ro.axon.dot.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.RefreshTokenEty;
import ro.axon.dot.domain.RefreshTokenRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenEty saveRefreshToken(RefreshTokenEty refreshTokenEty) {
    return refreshTokenRepository.save(refreshTokenEty);
  }

  public RefreshTokenEty findTokenByKeyId(String keyId) throws BusinessException {
    return refreshTokenRepository.findById(keyId)
        .orElseThrow(() -> {
          BusinessErrorCode errorCode = BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND;
          Map<String, Object> variables = new HashMap<>();
          variables.put("keyId", keyId);

          return new BusinessException(new RuntimeException(),
              new BusinessExceptionElement(errorCode, variables));
        });
  }
}
