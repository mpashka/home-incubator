// @tag:vertical-slice @tag:domain-model @tag:account-linking
package dev.homeincubator.lngedu.common;

import dev.homeincubator.lngedu.account.AppAccount;
import dev.homeincubator.lngedu.account.AppAccountRepository;
import dev.homeincubator.lngedu.account.ExternalIdentity;
import dev.homeincubator.lngedu.account.ExternalIdentityRepository;
import dev.homeincubator.lngedu.book.Book;
import dev.homeincubator.lngedu.book.BookRepository;
import dev.homeincubator.lngedu.book.BookText;
import dev.homeincubator.lngedu.book.BookTextRepository;
import dev.homeincubator.lngedu.user.User;
import dev.homeincubator.lngedu.user.UserLanguageSkill;
import dev.homeincubator.lngedu.user.UserLanguageSkillRepository;
import dev.homeincubator.lngedu.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Dev-only seed data for the vertical slice: two local learner profiles (each with per-language
 * skills) and one short test book for both 'sr' and 'en' with its text and length_chars.
 *
 * <p>Runs only under the {@code dev} profile, so production/test schemas stay clean and it never
 * runs during the (profile-less) slice tests. It is idempotent: each block checks existence before
 * inserting, so restarting a dev app does not duplicate rows.
 *
 * <p>Text provenance: the English book is the public-domain opening of Jane Austen's
 * <i>Pride and Prejudice</i> (1813); the Serbian book is obviously synthetic beginner prose written
 * for this repo. No copyrighted content, no secrets.
 */
@Component
@Profile("dev")
@Order(1)
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private static final String EN_TITLE = "Pride and Prejudice (opening)";
    private static final String SR_TITLE = "Marko i njegov dan (test)";

    private static final String EN_TEXT = """
            It is a truth universally acknowledged, that a single man in possession of a good \
            fortune, must be in want of a wife.

            However little known the feelings or views of such a man may be on his first entering a \
            neighbourhood, this truth is so well fixed in the minds of the surrounding families, \
            that he is considered the rightful property of some one or other of their daughters.

            "My dear Mr. Bennet," said his lady to him one day, "have you heard that Netherfield \
            Park is let at last?"

            Mr. Bennet replied that he had not.

            "But it is," returned she; "for Mrs. Long has just been here, and she told me all about \
            it."

            "Do you not want to know who has taken it?" cried his wife impatiently.

            "You want to tell me, and I have no objection to hearing it." This was invitation enough.\
            """;

    private static final String SR_TEXT = """
            Марко устаје рано ујутру. Он пије кафу и гледа кроз прозор. Напољу пада киша, али небо је \
            светло.

            После доручка Марко иде у школу. Школа је близу његове куће. На путу он среће свог \
            пријатеља Ивана.

            Иван има нову књигу. Књига говори о старом граду поред реке. Марко жели да прочита ту \
            књигу.

            Учитељ данас говори о историји. Ученици слушају и пишу у своје свеске. После часа деца \
            иду кући.

            Увече Марко чита књигу и учи нове речи. Он воли да учи српски језик сваки дан.\
            """;

    // Synthetic dev IdP identity so later auth phases (H/I) have data to resolve. Not a real secret.
    private static final String DEV_PROVIDER = "google";
    private static final String DEV_SUBJECT = "dev-google-sub-owner";

    private final UserRepository userRepository;
    private final UserLanguageSkillRepository skillRepository;
    private final BookRepository bookRepository;
    private final BookTextRepository bookTextRepository;
    private final AppAccountRepository accountRepository;
    private final ExternalIdentityRepository identityRepository;
    private final Clock clock;

    public DevDataSeeder(UserRepository userRepository,
                         UserLanguageSkillRepository skillRepository,
                         BookRepository bookRepository,
                         BookTextRepository bookTextRepository,
                         AppAccountRepository accountRepository,
                         ExternalIdentityRepository identityRepository,
                         Clock clock) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.bookRepository = bookRepository;
        this.bookTextRepository = bookTextRepository;
        this.accountRepository = accountRepository;
        this.identityRepository = identityRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Instant now = Instant.now(clock);
        seedLearners(now);
        seedAccountsAndLink(now);
        seedBook("en", EN_TITLE, "Jane Austen",
                "Jane Austen, Pride and Prejudice (1813), public domain", EN_TEXT, now);
        seedBook("sr", SR_TITLE, null, "Synthetic beginner test text", SR_TEXT, now);
    }

    /**
     * Seed one owner {@link AppAccount} that owns the seeded learner profiles, plus a synthetic dev
     * {@link ExternalIdentity} so later auth phases (H/I) have data to resolve (@tag:account-linking).
     *
     * <p>Idempotent: guarded on the {@code (provider, subject)} identity already existing. Also links
     * any still-unowned learners to the account, so it works on both a fresh DB and one that already
     * held learner rows from earlier phases.
     */
    private void seedAccountsAndLink(Instant now) {
        if (identityRepository.findByProviderAndSubject(DEV_PROVIDER, DEV_SUBJECT).isPresent()) {
            log.info("Dev seed: dev external_identity already present, skipping account seed");
            return;
        }

        AppAccount owner = new AppAccount();
        owner.setDisplayName("Dev Owner");
        owner.setRole("owner");
        owner.setCreatedAt(now);
        owner = accountRepository.save(owner);

        ExternalIdentity identity = new ExternalIdentity();
        identity.setAccountId(owner.getId());
        identity.setProvider(DEV_PROVIDER);
        identity.setSubject(DEV_SUBJECT);
        identity.setEmail("dev.owner@example.com");
        identity.setLinkedAt(now);
        identityRepository.save(identity);

        int linked = 0;
        for (User user : userRepository.findAll()) {
            if (user.getOwnerAccountId() == null) {
                user.setOwnerAccountId(owner.getId());
                userRepository.save(user);
                linked++;
            }
        }
        log.info("Dev seed: created owner account + dev identity, linked {} learner profile(s)", linked);
    }

    private void seedLearners(Instant now) {
        if (userRepository.count() > 0) {
            log.info("Dev seed: users already present, skipping learner seed");
            return;
        }
        User ana = createUser("Ana (dev)", "Europe/Belgrade", now);
        addSkill(ana, "sr", "A2", 30, 60, now);
        addSkill(ana, "en", "B1", 40, 80, now);

        User ben = createUser("Ben (dev)", "America/New_York", now);
        addSkill(ben, "en", "B2", 50, 90, now);
        addSkill(ben, "sr", "A1", 20, 40, now);
        log.info("Dev seed: inserted 2 learner profiles with language skills");
    }

    private User createUser(String name, String tz, Instant now) {
        User u = new User();
        u.setDisplayName(name);
        u.setTimezone(tz);
        u.setCreatedAt(now);
        return userRepository.save(u);
    }

    private void addSkill(User user, String language, String level, int min, int max, Instant now) {
        UserLanguageSkill skill = new UserLanguageSkill();
        skill.setUserId(user.getId());
        skill.setLanguage(language);
        skill.setLevel(level);
        skill.setBlockMinWords(min);
        skill.setBlockMaxWords(max);
        skill.setUpdatedAt(now);
        skillRepository.save(skill);
    }

    private void seedBook(String language, String title, String author, String source,
                          String content, Instant now) {
        if (!bookRepository.findByLanguage(language).isEmpty()) {
            log.info("Dev seed: a {} book already exists, skipping book seed", language);
            return;
        }
        Book book = new Book();
        book.setTitle(title);
        book.setLanguage(language);
        book.setAuthor(author);
        book.setSource(source);
        book.setCreatedAt(now);
        book = bookRepository.save(book);

        BookText text = new BookText();
        text.setBookId(book.getId());
        text.setContent(content);
        text.setLengthChars(content.length());
        bookTextRepository.save(text);
        log.info("Dev seed: inserted {} book '{}' ({} chars)", language, title, content.length());
    }
}
