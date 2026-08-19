package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.ERole;
import com.ecommerce.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
