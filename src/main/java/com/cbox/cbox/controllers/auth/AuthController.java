package com.cbox.cbox.controllers.auth;

import com.cbox.cbox.config.JwtService;
import com.cbox.cbox.dto.auth.LoginRequest;
import com.cbox.cbox.dto.auth.RefreshTokenRequest;
import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.RefreshToken;
import com.cbox.cbox.services.AuthService;
import com.cbox.cbox.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) {
    RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
        .map(refreshTokenService::verifyExpiration)
        .orElseThrow(() -> new RuntimeException("Nieprawidłowy refresh token"));
    AppUser user = refreshToken.getUser();
    String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
    return ResponseEntity.ok(AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken.getToken())
        .build());
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
    refreshTokenService.findByToken(request.getRefreshToken())
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .ifPresent(refreshTokenService::deleteByUser);
    return ResponseEntity.ok().build();
  }


  @PostMapping("/login")
//  @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody LoginRequest request
  ) {
    return ResponseEntity.ok(service.login(request));
  }
}
