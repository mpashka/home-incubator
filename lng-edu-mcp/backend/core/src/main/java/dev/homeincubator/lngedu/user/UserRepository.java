// @tag:domain-model @tag:account-linking
package dev.homeincubator.lngedu.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Learner profiles owned by an app_account (@tag:account-linking). */
    List<User> findByOwnerAccountId(UUID ownerAccountId);
}
