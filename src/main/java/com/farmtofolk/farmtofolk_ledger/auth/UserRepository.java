package com.farmtofolk.farmtofolk_ledger.auth;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmailIgnoreCase(String email);

  Optional<User> findByPhone(String phone);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

  boolean existsByPhone(String phone);

  boolean existsByPhoneAndIdNot(String phone, UUID id);

  boolean existsByRole(UserRole role);

  List<User> findByRoleIn(Collection<UserRole> roles);
}
