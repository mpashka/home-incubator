// @tag:vertical-slice @tag:auth
package dev.homeincubator.lngedu.book;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.security.CurrentAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Thin REST adapter for the book catalogue (shared across accounts). Delegates to {@link BookService}.
 * The catalogue itself is not account-scoped, but when a learner id is supplied to carry that
 * learner's reading progress the caller must own it (@tag:auth) — otherwise progress could leak.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book catalogue, optionally with a learner's reading progress")
public class BookController {

    private final BookService bookService;
    private final AccountService accountService;
    private final CurrentAccount currentAccount;

    public BookController(BookService bookService, AccountService accountService, CurrentAccount currentAccount) {
        this.bookService = bookService;
        this.accountService = accountService;
        this.currentAccount = currentAccount;
    }

    @GetMapping
    @Operation(summary = "List books, optionally filtered by language and carrying learner progress")
    public List<BookSummary> listBooks(
            @Parameter(description = "BCP 47 language filter (sr/en); omit to list all")
            @RequestParam(required = false) String language,
            @Parameter(description = "Learner id; when set, each book carries that learner's progress")
            @RequestParam(required = false) UUID userId) {
        if (userId != null) {
            accountService.assertOwnsLearner(currentAccount.accountId(), userId);
        }
        return bookService.listBooks(language, userId);
    }
}
