package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Membership Request endpoints */
@RestController
@RequestMapping("/api/v1/membership-requests")
@Tag(name = "Membership Request Controller", description = "Membership Request endpoints")
@RequiredArgsConstructor
public class MembershipRequestController {
    private final MembershipRequestService membershipRequestService;

    /**
     * Create a membership request
     *
     * @param newMembershipRequest ID of the book club and message for the request
     * @return Persisted version of the new membership request
     * @throws UserNotFoundException The user was not found
     * @throws BookClubNotFoundException The book club was not found
     */
    @PostMapping("/request-membership")
    @Operation(summary = "Request Membership", description = "Request membership in a book club")
    public ResponseEntity<MembershipRequestDTO> requestMembership(
            @RequestBody NewMembershipRequest newMembershipRequest)
            throws UserNotFoundException, BookClubNotFoundException {

        return ResponseEntity.ok(membershipRequestService.requestMembership(newMembershipRequest));
    }

    /**
     * See if a user has a pending request for a given book club
     *
     * @param bookClubName The name of the book club
     * @return A message indicating whether the user has a pending request
     * @throws UserNotFoundException The user was not found
     */
    @GetMapping("/has-pending-request/{bookClubName}")
    @Operation(summary = "Has Pending Request", description = "Check if the user has a pending request for a book club")
    public ResponseEntity<Boolean> hasPendingRequest(@PathVariable String bookClubName) throws UserNotFoundException {
        return ResponseEntity.ok(membershipRequestService.hasPendingRequest(bookClubName));
    }

    /**
     * Get all memberships for a given book club
     *
     * @param bookClubName The name of the book club
     * @return A page of membership requests for the book club
     * @throws UserNotFoundException The user was not found or was not in the book club
     * @throws BookClubNotFoundException The book club did not exist
     * @throws UnauthorizedBookClubActionException The user was not an admin of the book club
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    @GetMapping("/all-for-club/{bookClubName}")
    @Operation(summary = "Get All Requests", description = "Get all membership requests for a book club")
    public ResponseEntity<Page<MembershipRequestDTO>> getMembershipRequestsForBookClub(
            @PathVariable String bookClubName, @RequestParam int pageNum, @RequestParam int pageSize)
            throws UserNotFoundException, BookClubNotFoundException, UnauthorizedBookClubActionException,
                    PageSizeTooSmallException, PageSizeTooLargeException {

        return ResponseEntity.ok(
                membershipRequestService.getMembershipRequestsForBookClub(bookClubName, pageNum, pageSize));
    }

    /**
     * Approve or reject a membership request
     *
     * @param membershipRequestAction Approval or rejection of the request
     * @return The updated version of the membership request
     * @throws UserNotFoundException The user was not found or was not in the book club
     * @throws MembershipRequestNotFoundException The membership request was not found
     * @throws UnauthorizedBookClubActionException The user was not an admin of the book club
     * @throws BadBookClubActionException The membership request was not open or the target user was
     *     already a member
     */
    @PatchMapping("/review")
    @Operation(summary = "Review Request", description = "Approve or reject a membership request")
    public ResponseEntity<MembershipRequestDTO> reviewMembershipRequest(
            @RequestBody MembershipRequestAction membershipRequestAction)
            throws UserNotFoundException, MembershipRequestNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        return ResponseEntity.ok(membershipRequestService.reviewMembershipRequest(membershipRequestAction));
    }
}
