// @tag:auth @tag:account-linking
package dev.homeincubator.lngedu.account;

import dev.homeincubator.lngedu.user.User;
import dev.homeincubator.lngedu.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Account resolution (ADR 0002, @tag:account-linking). Turns an external OAuth identity
 * {@code (provider, subject, email)} into the linked {@link AppAccount}, creating a new account
 * plus {@link ExternalIdentity} on first sight and returning the existing account on repeat
 * (idempotent upsert keyed by the {@code (provider, subject)} UNIQUE constraint).
 *
 * <p>This is pure resolution logic used by later phases (H/I). It performs NO security
 * enforcement — no token validation, no allowlist, no ownership checks.
 */
@Service
public class AccountService {

    /** Role assigned to accounts auto-created on first login. */
    static final String DEFAULT_ROLE = "owner";

    private final AppAccountRepository accountRepository;
    private final ExternalIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AccountService(AppAccountRepository accountRepository,
                          ExternalIdentityRepository identityRepository,
                          UserRepository userRepository,
                          Clock clock) {
        this.accountRepository = accountRepository;
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /**
     * Resolve the app_account for an external identity, creating account + identity on first sight.
     * Idempotent by {@code (provider, subject)}: a repeat login returns the same account.
     */
    @Transactional
    public AppAccount resolveByExternalIdentity(String provider, String subject, String email) {
        var existing = identityRepository.findByProviderAndSubject(provider, subject);
        if (existing.isPresent()) {
            return loadAccount(existing.get().getAccountId());
        }

        Instant now = Instant.now(clock);
        AppAccount account = new AppAccount();
        account.setDisplayName(email != null ? email : provider + ":" + subject);
        account.setRole(DEFAULT_ROLE);
        account.setCreatedAt(now);
        account = accountRepository.save(account);

        ExternalIdentity identity = new ExternalIdentity();
        identity.setAccountId(account.getId());
        identity.setProvider(provider);
        identity.setSubject(subject);
        identity.setEmail(email);
        identity.setLinkedAt(now);
        try {
            identityRepository.save(identity);
        } catch (DataIntegrityViolationException race) {
            // Lost a concurrent race on the (provider, subject) UNIQUE key: return the winner.
            return loadAccount(identityRepository.findByProviderAndSubject(provider, subject)
                    .orElseThrow(() -> race)
                    .getAccountId());
        }
        return account;
    }

    /** Learner profiles owned by the given app_account (@tag:account-linking). */
    @Transactional(readOnly = true)
    public List<User> listOwnedLearners(UUID accountId) {
        return userRepository.findByOwnerAccountId(accountId);
    }

    private AppAccount loadAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException(
                        "external_identity references missing app_account " + accountId));
    }
}
