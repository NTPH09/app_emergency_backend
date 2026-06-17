package com.emergency.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.emergency.backend.entity.Provider;
import com.emergency.backend.entity.User;
import com.emergency.backend.entity.UserProvider;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderId(Provider provider, String providerId);
    List<UserProvider> findByUser(User user);
    boolean existsByUserAndProvider(User user, Provider provider);
}