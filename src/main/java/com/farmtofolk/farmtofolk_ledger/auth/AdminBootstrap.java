package com.farmtofolk.farmtofolk_ledger.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final boolean enabled;
  private final String name;
  private final String phone;
  private final String email;
  private final String password;

  public AdminBootstrap(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.bootstrap.admin.enabled:false}") boolean enabled,
      @Value("${app.bootstrap.admin.name:FarmToFolk Admin}") String name,
      @Value("${app.bootstrap.admin.phone:}") String phone,
      @Value("${app.bootstrap.admin.email:}") String email,
      @Value("${app.bootstrap.admin.password:}") String password) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.enabled = enabled;
    this.name = name;
    this.phone = phone;
    this.email = email;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!enabled || userRepository.existsByRole(UserRole.ADMIN)) {
      return;
    }
    if (password == null || password.length() < 8 || (isBlank(email) && isBlank(phone))) {
      throw new IllegalStateException(
          "Bootstrap admin requires a password of at least 8 characters and an email or phone");
    }

    // Bootstrap runs once only when explicitly enabled and no administrator exists.
    User admin = new User();
    admin.setName(name);
    admin.setEmail(isBlank(email) ? null : email.trim());
    admin.setPhone(isBlank(phone) ? null : phone.trim());
    admin.setPasswordHash(passwordEncoder.encode(password));
    admin.setRole(UserRole.ADMIN);
    admin.setActive(true);
    userRepository.save(admin);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
