package com.cbox.cbox.services;

import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.RefreshToken;
import com.cbox.cbox.repositories.RefreshTokenRepository;
import com.cbox.cbox.repositories.UserRepository;
import com.cbox.cbox.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  public RefreshToken createRefreshToken(String email) {
    AppUser user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));
    RefreshToken refreshToken = RefreshToken.builder()
        .token(jwtService.generateRefreshToken())
        .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
        .user(user)
        .build();
    refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(Instant.now())) {
      refreshTokenRepository.delete(token);
      throw new RuntimeException("Refresh token wygasł");
    }
    return token;
  }

  @Transactional
  public void deleteByUser(AppUser user) {
    refreshTokenRepository.deleteByUser(user);
  }
}