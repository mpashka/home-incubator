package dev.homeincubator.lngedu.account;

import dev.homeincubator.lngedu.user.User;
import dev.homeincubator.lngedu.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Resolution logic for {@link AccountService} (@tag:account-linking): create-on-first-seen and
 * return-existing-on-repeat. Pure Mockito, no Spring/DB.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-09T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AppAccountRepository accountRepository;
    @Mock
    private ExternalIdentityRepository identityRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void firstSeenIdentity_createsAccountAndIdentity() {
        AccountService service = new AccountService(accountRepository, identityRepository, userRepository, clock);

        when(identityRepository.findByProviderAndSubject("google", "sub-1")).thenReturn(Optional.empty());
        when(accountRepository.save(any(AppAccount.class))).thenAnswer(inv -> {
            AppAccount a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AppAccount result = service.resolveByExternalIdentity("google", "sub-1", "kid@example.com");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getRole()).isEqualTo(AccountService.DEFAULT_ROLE);
        assertThat(result.getDisplayName()).isEqualTo("kid@example.com");

        ArgumentCaptor<ExternalIdentity> identityCaptor = ArgumentCaptor.forClass(ExternalIdentity.class);
        verify(identityRepository).save(identityCaptor.capture());
        ExternalIdentity saved = identityCaptor.getValue();
        assertThat(saved.getProvider()).isEqualTo("google");
        assertThat(saved.getSubject()).isEqualTo("sub-1");
        assertThat(saved.getEmail()).isEqualTo("kid@example.com");
        assertThat(saved.getAccountId()).isEqualTo(result.getId());
    }

    @Test
    void repeatIdentity_returnsExistingAccountWithoutCreating() {
        AccountService service = new AccountService(accountRepository, identityRepository, userRepository, clock);

        UUID accountId = UUID.randomUUID();
        ExternalIdentity existing = new ExternalIdentity();
        existing.setId(UUID.randomUUID());
        existing.setAccountId(accountId);
        existing.setProvider("google");
        existing.setSubject("sub-1");

        AppAccount account = new AppAccount();
        account.setId(accountId);
        account.setRole("owner");

        when(identityRepository.findByProviderAndSubject("google", "sub-1")).thenReturn(Optional.of(existing));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AppAccount result = service.resolveByExternalIdentity("google", "sub-1", "kid@example.com");

        assertThat(result.getId()).isEqualTo(accountId);
        // No account or identity created on repeat.
        verify(accountRepository, never()).save(any(AppAccount.class));
        verify(identityRepository, never()).save(any(ExternalIdentity.class));
    }

    @Test
    void listOwnedLearners_delegatesToRepository() {
        AccountService service = new AccountService(accountRepository, identityRepository, userRepository, clock);
        UUID accountId = UUID.randomUUID();
        User learner = new User();
        learner.setId(UUID.randomUUID());
        learner.setOwnerAccountId(accountId);
        when(userRepository.findByOwnerAccountId(accountId)).thenReturn(List.of(learner));

        List<User> owned = service.listOwnedLearners(accountId);

        assertThat(owned).containsExactly(learner);
        verify(userRepository, times(1)).findByOwnerAccountId(accountId);
    }
}
