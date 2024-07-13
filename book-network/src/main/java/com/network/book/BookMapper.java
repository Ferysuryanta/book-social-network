package com.network.book;

import com.network.history.BookTransactionHistory;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    public Book toBook(BookRequest bookRequest) {
        return Book.builder()
                .id(bookRequest.id())
                .title(bookRequest.title())
                .authorName(bookRequest.authorName())
                .isbn(bookRequest.isbn())
                .synopsis(bookRequest.synopsis())
                .archived(false)
                .shareable(bookRequest.shareable())
                .build();
    }

    public BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .rate(book.getRate())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().getFullName())
//                .cover()
                .build();
    }

    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory transactionHistory) {
        return BorrowedBookResponse.builder()
                .id(transactionHistory.getBook().getId())
                .title(transactionHistory.getBook().getTitle())
                .authorName(transactionHistory.getBook().getAuthorName())
                .isbn(transactionHistory.getBook().getIsbn())
                .rate(transactionHistory.getBook().getRate())
                .returned(transactionHistory.isReturned())
                .returnedApprove(transactionHistory.isReturnedApproved())
                .build();
    }
}
