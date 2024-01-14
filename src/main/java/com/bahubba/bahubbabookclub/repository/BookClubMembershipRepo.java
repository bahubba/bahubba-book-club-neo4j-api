package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository for the {@link BookClubMembership} entity */
@Repository
public interface BookClubMembershipRepo extends JpaRepository<BookClubMembership, UUID> {
    Optional<BookClubMembership> findByBookClubNameAndUserId(String bookClubName, UUID userId);

    Optional<BookClubMembership> findByBookClubNameAndUserIdAndDepartedIsNull(String bookClubName, UUID userId);

    Optional<BookClubMembership> findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
            String bookClubName, UUID userId, BookClubRole clubRole);

    Optional<BookClubMembership> findByBookClubIdAndUserId(UUID bookClubId, UUID userId);

    Boolean existsByBookClubIdAndUserId(UUID bookClubId, UUID userId);

    Optional<BookClubMembership> findByBookClubNameAndClubRoleAndUserId(
            String bookClubName, BookClubRole role, UUID userId);

    Page<BookClubMembership> findAllByBookClubNameOrderByJoined(String bookClubName, Pageable pageable);

    Optional<BookClubMembership> findByBookClubNameAndUserIdAndIsOwnerTrue(String bookClubName, UUID userId);

    List<BookClubMembership> findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(
            UUID bookClubId, List<UUID> userIds);
}
