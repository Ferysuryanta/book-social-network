package com.network.history;

import com.network.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {

    @Query("""
        SELECT history
        FROM BookTransactionHistory history
        WHERE history.user.id = :userId
        """)
    Page<BookTransactionHistory> findAllBorrowedBooks(PageRequest pageable, Integer userId);
    @Query("""
        SELECT history
        FROM BookTransactionHistory history
        WHERE history.book.owner.id = :userId
        """)
    Page<BookTransactionHistory> findAllReturnedBooks(PageRequest pageable, Integer userId);

    @Query("""
        SELECT
            (COUNT (*)  > 0) AS isBorrowed
        FROM BookTransactionHistory history
        WHERE history.user.id = :userId
        AND history.book.id = :bookId
        AND history.returnedApproved = false
        """)
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);

    @Query("""
        SELECT transaction
        FROM BookTransactionHistory transaction
        WHERE transaction.book.owner.id = :userId
        AND transaction.book.id = :bookId
        AND transaction.returned = true
        AND transaction.returnedApproved = false
    """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer userId);

    @Query("""
       SELECT transaction
        FROM BookTransactionHistory transaction
        WHERE transaction.user.id = :userId
        AND transaction.book.id = :bookId
        AND transaction.returned = true
        AND transaction.returnedApproved =false
    """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(@Param("bookId") Integer bookId, @Param("userId") Integer userId);
}
