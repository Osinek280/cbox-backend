package com.cbox.cbox.services;

import com.cbox.cbox.controllers.auth.AuthenticationResponse;
import com.cbox.cbox.controllers.auth.RegisterRequest;
import com.cbox.cbox.dto.auth.LoginRequest;
import com.cbox.cbox.entities.user.*;
import com.cbox.cbox.repositories.UserRepository;
import com.cbox.cbox.config.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.Role;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;

  public AuthenticationResponse register(RegisterRequest request) {
    AppUser user = AppUser.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.ADMIN)
        .build();
    repository.save(user);

    String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken.getToken())
        .build();
  }

  public AuthenticationResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        )
    );

    AppUser user = repository.findByEmail(request.email())
        .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));

    String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken.getToken())
        .build();
  }
}
