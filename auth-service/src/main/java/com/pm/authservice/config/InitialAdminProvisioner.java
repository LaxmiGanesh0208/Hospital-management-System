package com.pm.authservice.config;

import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates one initial administrator from private deployment secrets when none exists. */
@Component
@Profile("prod")
public class InitialAdminProvisioner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(InitialAdminProvisioner.class);
  private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "SUPER_ADMIN");

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final String email;
  private final String password;

  public InitialAdminProvisioner(UserRepository users, PasswordEncoder passwordEncoder,
      @Value("${DEMO_ADMIN_EMAIL:}") String email,
      @Value("${DEMO_ADMIN_PASSWORD:}") String password) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    this.password = password == null ? "" : password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!users.findByRoleInOrderByFullNameAsc(ADMIN_ROLES).isEmpty()) return;

    int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
    if (!email.contains("@") || passwordBytes < 24 || passwordBytes > 72) {
      throw new IllegalStateException(
          "No administrator exists. Set DEMO_ADMIN_EMAIL and a private DEMO_ADMIN_PASSWORD (24–72 UTF-8 bytes) to bootstrap one.");
    }
    if (users.existsByEmailIgnoreCase(email)) {
      throw new IllegalStateException("The initial administrator email already belongs to a non-administrator account.");
    }

    User admin = new User();
    admin.setEmail(email);
    admin.setPassword(passwordEncoder.encode(password));
    admin.setFullName("Demo Administrator");
    admin.setRole("ADMIN");
    users.save(admin);
    log.info("Created the first administrator from the private deployment environment.");
  }
}
