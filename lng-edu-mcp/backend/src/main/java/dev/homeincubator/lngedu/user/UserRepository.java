// @tag:domain-model
package dev.homeincubator.lngedu.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
