package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** JPA Repository for the {@link MembershipRequest} entity */
public interface MembershipRequestRepo extends JpaRepository<MembershipRequest, UUID> {
    Boolean existsByBookClubNameAndUserIdAndStatus(
            final String bookClubName, final UUID userId, final RequestStatus status);

    Boolean existsByBookClubNameAndUserIdAndStatusIn(
            final String bookClubName, final UUID userId, final List<RequestStatus> statuses);

    Page<MembershipRequest> findAllByBookClubIdOrderByRequestedDesc(final UUID bookClubId, Pageable pageable);

    @Modifying
    @Query(
            "UPDATE MembershipRequest mr SET mr.status = :status, mr.role = :role, mr.reviewMessage = :reviewMessage, mr.reviewed = :reviewed WHERE mr.id = :id")
    Integer reviewMembershipRequest(
            final UUID id,
            final RequestStatus status,
            final BookClubRole role,
            final String reviewMessage,
            final LocalDateTime reviewed);
}
