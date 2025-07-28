package com.cbox.cbox.services;

import com.cbox.cbox.controllers.auth.AuthenticationResponse;
import com.cbox.cbox.controllers.auth.RegisterRequest;
import com.cbox.cbox.dto.auth.LoginRequest;
import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.Role;
import com.cbox.cbox.repositories.UserRepository;
import com.cbox.cbox.config.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
    AppUser user = new AppUser();
    user.setEmail(request.getFirstname());
    user.setLastname(request.getLastname());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.ADMIN);

    repository.save(user);

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return AuthenticationResponse.builder()
        .token(token)
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
        .orElseThrow();

    String token = jwtService.generateToken(user.getEmail(), user.getRole());

    return AuthenticationResponse.builder()
        .token(token)
        .build();
  }
}
