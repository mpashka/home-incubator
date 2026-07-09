// @tag:domain-model
package dev.homeincubator.lngedu.book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByLanguage(String language);
}
