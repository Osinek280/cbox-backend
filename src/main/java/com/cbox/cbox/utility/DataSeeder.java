package com.cbox.cbox.utility;

import com.cbox.cbox.entities.user.AppUser;
import com.cbox.cbox.entities.user.Role;
import com.cbox.cbox.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class DataSeeder {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;
  @PostConstruct
  public void seedData() {
    createUsers();
  }

  private void createUsers() {
    AppUser user = AppUser.builder()
        .email("admin@admin.com")
        .firstname("Adam")
        .lastname("Admin")
        .password(passwordEncoder.encode("admin"))
        .role(Role.ADMIN)
        .build();

    userRepository.save(user);
  }
}
