// @tag:domain-model @tag:reading-block
package dev.homeincubator.lngedu.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BookTextRepository extends JpaRepository<BookText, UUID> {

    /** Length only, so book listings don't load the full text column. */
    @Query("select bt.lengthChars from BookText bt where bt.bookId = :bookId")
    Optional<Integer> findLengthByBookId(@Param("bookId") UUID bookId);
}
