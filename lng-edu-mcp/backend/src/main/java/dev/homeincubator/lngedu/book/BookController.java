// @tag:vertical-slice
package dev.homeincubator.lngedu.book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Thin REST adapter for the book catalogue. Delegates to {@link BookService}. */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book catalogue, optionally with a learner's reading progress")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "List books, optionally filtered by language and carrying learner progress")
    public List<BookSummary> listBooks(
            @Parameter(description = "BCP 47 language filter (sr/en); omit to list all")
            @RequestParam(required = false) String language,
            @Parameter(description = "Learner id; when set, each book carries that learner's progress")
            @RequestParam(required = false) UUID userId) {
        return bookService.listBooks(language, userId);
    }
}
