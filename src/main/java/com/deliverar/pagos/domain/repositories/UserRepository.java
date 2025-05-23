package com.deliverar.pagos.domain.repositories;

import com.deliverar.pagos.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    void deleteByEmailIgnoreCase(String email);
}
