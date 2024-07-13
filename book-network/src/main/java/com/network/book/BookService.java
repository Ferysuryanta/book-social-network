package com.network.book;

import com.network.common.PageResponse;
import com.network.exception.OperationNotPermittedException;
import com.network.history.BookTransactionHistory;
import com.network.history.BookTransactionHistoryRepository;
import com.network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        var user = ((User) connectedUser.getPrincipal());
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var returnedBook = historyRepository.findAllReturnedBooks(pageable, user.getId());
        var returnedBookResponse = returnedBook.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                returnedBookResponse,
                returnedBook.getNumber(),
                returnedBook.getSize(),
                returnedBook.getTotalElements(),
                returnedBook.getTotalPages(),
                returnedBook.isFirst(),
                returnedBook.isLast()
        );
    }

    public Integer updateShareAbleStatus(Integer bookId, Authentication connectedUser) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        var user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            // throw an exception
            throw new OperationNotPermittedException("You cannot other update books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        var user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            // throw an exception
            throw new OperationNotPermittedException("You cannot update others books archive status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBooks(Integer bookId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be borrowed since archived or not shareable");
        }
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        var isAlreadyBorrowed = historyRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }
        var transactionBookHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnedApproved(false)
                .build();
        return historyRepository.save(transactionBookHistory).getId();
    }

    public Integer returnedBorrowBooks(Integer bookId, Authentication connectedUser) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be borrowed since archived or not shareable");
        }

        var user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        var bookTransactionHistory = historyRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

        bookTransactionHistory.setReturned(true);
        return historyRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBooks(Integer bookId, Authentication connectedUser) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be borrowed since archived or not shareable");
        }
        var user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        var bookTransactionHistory = historyRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));
        bookTransactionHistory.setReturnedApproved(true);
        return historyRepository.save(bookTransactionHistory).getId();
    }
}
