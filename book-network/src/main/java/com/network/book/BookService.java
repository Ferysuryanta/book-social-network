package com.network.book;

import com.network.common.PageResponse;
import com.network.history.BookTransactionHistoryRepository;
import com.network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.network.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository historyRepository;
    private final BookMapper bookMapper;
    public Integer save(BookRequest bookRequest, Authentication connectedUser) {
        var user = ((User) connectedUser.getPrincipal());
        var book = bookMapper.toBook(bookRequest);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No Book found with ID: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        var user = ((User) connectedUser.getPrincipal());
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        var bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        var user = ((User) connectedUser.getPrincipal());
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var books = bookRepository.findAll(withOwnerId(user.getId()), pageable);
        var bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooksByOwner(int page, int size, Authentication connectedUser) {
        var user = ((User) connectedUser.getPrincipal());
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var bookTransactionHistories = historyRepository.findAllBorrowedBooks(pageable, user.getId());
        var borrowedBookResponse = bookTransactionHistories.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                borrowedBookResponse,
                bookTransactionHistories.getNumber(),
                bookTransactionHistories.getSize(),
                bookTransactionHistories.getTotalElements(),
                bookTransactionHistories.getTotalPages(),
                bookTransactionHistories.isFirst(),
                bookTransactionHistories.isLast()
        );
    }
}
