package dev.homeincubator.lngedu.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Profiles use case: list learners with their language skills. */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final UserLanguageSkillRepository skillRepository;

    public ProfileService(UserRepository userRepository, UserLanguageSkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<LearnerSummary> listLearners() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Learner profiles owned by the given app_account (@tag:auth, @tag:account-linking). The
     * adapter resolves {@code accountId} from the authenticated token so a caller only ever sees
     * its own learners (closes AGENTS rule 5).
     */
    @Transactional(readOnly = true)
    public List<LearnerSummary> listLearnersOwnedBy(UUID accountId) {
        return userRepository.findByOwnerAccountId(accountId).stream()
                .map(this::toSummary)
                .toList();
    }

    private LearnerSummary toSummary(User user) {
        return new LearnerSummary(
                user.getId(),
                user.getDisplayName(),
                user.getTimezone(),
                skillRepository.findByUserId(user.getId()).stream()
                        .map(s -> new LearnerSummary.LanguageSkillSummary(
                                s.getLanguage(), s.getLevel(),
                                s.getBlockMinWords(), s.getBlockMaxWords()))
                        .toList());
    }
}
