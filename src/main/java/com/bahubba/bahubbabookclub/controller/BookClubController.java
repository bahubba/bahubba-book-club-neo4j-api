package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.S3ImageDTO;
import com.bahubba.bahubbabookclub.model.payload.BookClubPayload;
import com.bahubba.bahubbabookclub.model.payload.BookClubSearch;
import com.bahubba.bahubbabookclub.service.BookClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Book Club endpoints */
@RestController
@RequestMapping("/api/v1/book-clubs")
@Tag(name = "Book Club Controller", description = "Book Club endpoints")
@RequiredArgsConstructor
public class BookClubController {

    private final BookClubService bookClubService;

    /**
     * Creates a book club
     *
     * @param newBookClub Metadata for a new book club
     * @return Persisted version of the new book club
     * @throws UserNotFoundException The user was not found
     * @throws BadBookClubActionException The book club's name was a reserved word
     */
    @PostMapping("/create")
    @Operation(summary = "Create", description = "Creates a new book club")
    public ResponseEntity<BookClubDTO> create(@RequestBody BookClubPayload newBookClub)
            throws UserNotFoundException, BadBookClubActionException {
        return ResponseEntity.ok(bookClubService.create(newBookClub));
    }

    /**
     * Updates a book club
     *
     * @param updatedBookClub New book club metadata
     * @return Persisted version of the new book club
     * @throws UserNotFoundException The user was not found
     * @throws UnauthorizedBookClubActionException The book club was not found where the reader was an active admin
     */
    @Operation(summary = "Update", description = "Updates a book club's metadata")
    @PatchMapping("/update")
    public ResponseEntity<BookClubDTO> update(@RequestBody BookClubPayload updatedBookClub)
            throws UserNotFoundException, UnauthorizedBookClubActionException {

        return ResponseEntity.ok(bookClubService.update(updatedBookClub));
    }

    /**
     * Retrieves a book club by ID
     *
     * @param id The book club's ID
     * @return The book club's info
     * @throws BookClubNotFoundException The book club was not found
     * @throws UserNotFoundException The user was not found
     * @throws MembershipNotFoundException The user was not a member of the book club
     */
    @GetMapping("/by-id/{id}")
    @Operation(summary = "Get by ID", description = "Retrieves a book club by ID")
    public ResponseEntity<BookClubDTO> getByID(@PathVariable UUID id)
            throws BookClubNotFoundException, UserNotFoundException, MembershipNotFoundException {

        return ResponseEntity.ok(bookClubService.findByID(id));
    }

    /**
     * Retrieves a book club by name
     *
     * @param name The book club's name
     * @return The book club's info
     * @throws BookClubNotFoundException The book club was not found
     * @throws UserNotFoundException The user was not found
     * @throws MembershipNotFoundException The user was not a member of the book club
     */
    @GetMapping("/by-name/{name}")
    @Operation(summary = "Get by Name", description = "Retrieves a book club by name")
    public ResponseEntity<BookClubDTO> getByName(@PathVariable String name)
            throws BookClubNotFoundException, UserNotFoundException, MembershipNotFoundException {

        return ResponseEntity.ok(bookClubService.findByName(name));
    }

    /**
     * Retrieves all book clubs for a given user
     *
     * @return A page of all book clubs that the requesting user has a role in
     * @throws UserNotFoundException The user wasn't found in the DB
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    @GetMapping("/all-for-user")
    @Operation(summary = "Get All for User", description = "Retrieves all book clubs for a given user")
    public ResponseEntity<Page<BookClubDTO>> getAllForUser(@RequestParam int pageNum, @RequestParam int pageSize)
            throws UserNotFoundException, PageSizeTooSmallException, PageSizeTooLargeException {

        return ResponseEntity.ok(bookClubService.findAllForUser(pageNum, pageSize));
    }

    // TODO - pre-authorize this endpoint to only allow admins to access it
    /**
     * Retrieves all book clubs
     *
     * @return A page of all book clubs
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    @GetMapping("/all")
    @Operation(summary = "Get All", description = "Retrieves all book clubs")
    public ResponseEntity<Page<BookClubDTO>> getAll(@RequestParam int pageNum, @RequestParam int pageSize)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        return ResponseEntity.ok(bookClubService.findAll(pageNum, pageSize));
    }

    /**
     * Disbands (soft deletes) a book club
     *
     * @param id The book club's ID
     * @return The updated book club
     * @throws UserNotFoundException The user was not found
     * @throws MembershipNotFoundException The user was not a member of the book club
     * @throws UnauthorizedBookClubActionException The user was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    @DeleteMapping("/disband/{id}")
    @Operation(summary = "Disband by ID", description = "Disbands (soft deletes) a book club by ID")
    public ResponseEntity<BookClubDTO> disbandBookClub(@PathVariable UUID id)
            throws UserNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {
        return ResponseEntity.ok(bookClubService.disbandBookClubByID(id));
    }

    /**
     * Disbands (soft deletes) a book club by name
     *
     * @param name The book club's name
     * @return The updated book club
     * @throws UserNotFoundException The user was not found
     * @throws MembershipNotFoundException The user was not a member of the book club
     * @throws UnauthorizedBookClubActionException The user was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    @DeleteMapping("/disband-by-name/{name}")
    @Operation(summary = "Disband by Name", description = "Disbands (soft deletes) a book club by name")
    public ResponseEntity<BookClubDTO> disbandBookClubByName(@PathVariable String name)
            throws UserNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        return ResponseEntity.ok(bookClubService.disbandBookClubByName(name));
    }

    /**
     * Searches for book clubs by name
     *
     * @param bookClubSearch Search string and pagination info
     * @return A page of book clubs that match the search string
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    @PostMapping(value = "/search")
    @Operation(summary = "Search", description = "Searches for book clubs by name")
    public ResponseEntity<Page<BookClubDTO>> search(@RequestBody @NotNull BookClubSearch bookClubSearch)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        return ResponseEntity.ok(bookClubService.search(
                bookClubSearch.getSearchTerm(), bookClubSearch.getPageNum(), bookClubSearch.getPageSize()));
    }

    /**
     * Gets pre-signed URLs for all stock book club images
     *
     * @return A list of pre-signed URLs for all stock book club images
     */
    @GetMapping(value = "/stock-images")
    @Operation(summary = "Get Stock Images", description = "Gets pre-signed URLs for all stock book club images")
    public ResponseEntity<List<S3ImageDTO>> getPreSignedStockBookClubImageURLs() {
        return ResponseEntity.ok(bookClubService.getStockBookClubImages());
    }
}
