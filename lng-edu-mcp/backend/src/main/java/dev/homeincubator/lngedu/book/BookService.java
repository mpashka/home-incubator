package dev.homeincubator.lngedu.book;

import dev.homeincubator.lngedu.reading.ReadingProgress;
import dev.homeincubator.lngedu.reading.ReadingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Books use case: list books (optionally by language) with a learner's reading progress. */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookTextRepository bookTextRepository;
    private final ReadingProgressRepository readingProgressRepository;

    public BookService(BookRepository bookRepository,
                       BookTextRepository bookTextRepository,
                       ReadingProgressRepository readingProgressRepository) {
        this.bookRepository = bookRepository;
        this.bookTextRepository = bookTextRepository;
        this.readingProgressRepository = readingProgressRepository;
    }

    /**
     * @param language optional BCP 47 filter ({@code sr}/{@code en}); {@code null} lists all
     * @param userId   optional learner; when set, each book carries that learner's progress
     */
    @Transactional(readOnly = true)
    public List<BookSummary> listBooks(String language, UUID userId) {
        List<Book> books = (language == null || language.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByLanguage(language);

        return books.stream()
                .map(book -> new BookSummary(
                        book.getId(),
                        book.getTitle(),
                        book.getLanguage(),
                        book.getAuthor(),
                        book.getSource(),
                        progressFor(userId, book.getId())))
                .toList();
    }

    private BookSummary.Progress progressFor(UUID userId, UUID bookId) {
        if (userId == null) {
            return null;
        }
        int lengthChars = bookTextRepository.findLengthByBookId(bookId).orElse(0);
        int position = readingProgressRepository.findByUserIdAndBookId(userId, bookId)
                .map(ReadingProgress::getPositionChar)
                .orElse(0);
        double percent = lengthChars > 0 ? (100.0 * position / lengthChars) : 0.0;
        return new BookSummary.Progress(position, lengthChars, percent);
    }
}
