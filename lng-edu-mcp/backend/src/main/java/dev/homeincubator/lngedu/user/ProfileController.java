// @tag:vertical-slice
package dev.homeincubator.lngedu.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thin REST adapter for learner profiles. Delegates to {@link ProfileService}. */
@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profiles", description = "Local learner profiles and their per-language reading skills")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "List learner profiles with their language skills")
    public List<LearnerSummary> listProfiles() {
        return profileService.listLearners();
    }
}
