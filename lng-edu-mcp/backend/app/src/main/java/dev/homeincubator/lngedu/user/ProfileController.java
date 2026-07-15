// @tag:vertical-slice @tag:auth
package dev.homeincubator.lngedu.user;

import dev.homeincubator.lngedu.security.CurrentAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST adapter for learner profiles. Delegates to {@link ProfileService} and returns ONLY the
 * authenticated account's learners (@tag:auth) — the account is taken from the token, never a param.
 */
@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profiles", description = "Local learner profiles and their per-language reading skills")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentAccount currentAccount;

    public ProfileController(ProfileService profileService, CurrentAccount currentAccount) {
        this.profileService = profileService;
        this.currentAccount = currentAccount;
    }

    @GetMapping
    @Operation(summary = "List the authenticated account's learner profiles with their language skills")
    public List<LearnerSummary> listProfiles() {
        return profileService.listLearnersOwnedBy(currentAccount.accountId());
    }
}
