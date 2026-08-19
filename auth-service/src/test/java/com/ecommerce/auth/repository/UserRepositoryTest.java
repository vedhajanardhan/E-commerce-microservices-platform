package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.ERole;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @Test
    void findByUsername_returnsUserWithRoles() {
        Role userRole = roleRepository.save(new Role(ERole.ROLE_USER));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username("vedha")
                .email("vedha@example.com")
                .password("hashed")
                .firstName("Vedha")
                .lastName("J")
                .roles(roles)
                .build();
        entityManager.persistAndFlush(user);
        entityManager.clear();

        Optional<User> found = userRepository.findByUsername("vedha");

        assertTrue(found.isPresent());
        assertEquals("vedha@example.com", found.get().getEmail());
        assertEquals(1, found.get().getRoles().size());
        assertTrue(found.get().getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_USER));
    }

    @Test
    void existsByUsername_andExistsByEmail_reflectPersistedState() {
        User user = User.builder()
                .username("uniqueuser")
                .email("unique@example.com")
                .password("hashed")
                .roles(new HashSet<>())
                .build();
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByUsername("uniqueuser"));
        assertTrue(userRepository.existsByEmail("unique@example.com"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void findByUsernameOrEmail_matchesEitherField() {
        User user = User.builder()
                .username("dualcheck")
                .email("dual@example.com")
                .password("hashed")
                .roles(new HashSet<>())
                .build();
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.findByUsernameOrEmail("dualcheck", "dualcheck").isPresent());
        assertTrue(userRepository.findByUsernameOrEmail("dual@example.com", "dual@example.com").isPresent());
    }
}
