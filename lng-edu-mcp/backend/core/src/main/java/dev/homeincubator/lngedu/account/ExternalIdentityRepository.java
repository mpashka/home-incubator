// @tag:auth @tag:account-linking
package dev.homeincubator.lngedu.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, UUID> {

    /** Resolve an IdP identity to its linked row; the (provider, subject) UNIQUE key backs this. */
    Optional<ExternalIdentity> findByProviderAndSubject(String provider, String subject);
}
