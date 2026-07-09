// @tag:auth @tag:account-linking
package dev.homeincubator.lngedu.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppAccountRepository extends JpaRepository<AppAccount, UUID> {
}
