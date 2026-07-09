// @tag:domain-model @tag:reading-block
package dev.homeincubator.lngedu.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLanguageSkillRepository extends JpaRepository<UserLanguageSkill, UUID> {

    Optional<UserLanguageSkill> findByUserIdAndLanguage(UUID userId, String language);

    List<UserLanguageSkill> findByUserId(UUID userId);
}
