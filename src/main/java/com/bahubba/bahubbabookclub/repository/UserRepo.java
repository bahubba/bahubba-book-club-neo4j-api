package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository for the {@link User} entity */
@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(final String username);

    Optional<User> findByUsernameAndDepartedIsNull(final String username);

    Optional<User> findByEmail(final String email);

    Optional<User> findByUsernameOrEmail(final String username, final String email);

    boolean existsByUsername(final String username);

    boolean existsByEmail(final String email);

    boolean existsByUsernameOrEmail(final String username, final String email);
}
