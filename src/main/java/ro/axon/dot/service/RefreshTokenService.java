package ro.axon.dot.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.entity.RefreshTokenEty;
import ro.axon.dot.domain.repositories.RefreshTokenRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshTokenEty saveRefreshToken(RefreshTokenEty refreshTokenEty) {
    return refreshTokenRepository.save(refreshTokenEty);
  }

  public RefreshTokenEty findTokenByKeyId(String keyId) throws BusinessException {
    return refreshTokenRepository.findById(keyId)
        .orElseThrow(() -> new BusinessException(BusinessExceptionElement
            .builder()
            .errorDescription(BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND)
            .build()));
  }
}
