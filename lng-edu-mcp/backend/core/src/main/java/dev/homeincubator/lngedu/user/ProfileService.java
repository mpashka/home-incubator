package dev.homeincubator.lngedu.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .map(user -> new LearnerSummary(
                        user.getId(),
                        user.getDisplayName(),
                        user.getTimezone(),
                        skillRepository.findByUserId(user.getId()).stream()
                                .map(s -> new LearnerSummary.LanguageSkillSummary(
                                        s.getLanguage(), s.getLevel(),
                                        s.getBlockMinWords(), s.getBlockMaxWords()))
                                .toList()))
                .toList();
    }
}
