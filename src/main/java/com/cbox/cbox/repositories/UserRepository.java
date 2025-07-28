package com.cbox.cbox.repositories;

import com.cbox.cbox.entities.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<AppUser, Integer> {
  Optional<AppUser> findByEmail(String email);
}
