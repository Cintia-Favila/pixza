package org.switf.pixza.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.switf.pixza.models.UserModel;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByUsername(String username);
}

