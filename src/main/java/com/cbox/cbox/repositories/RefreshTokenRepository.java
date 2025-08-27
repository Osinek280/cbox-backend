package com.cbox.cbox.repositories;


import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  void deleteByUser(AppUser user);
}