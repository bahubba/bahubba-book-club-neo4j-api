package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** JPA Repository for the {@link BookClub} entity */
@Repository
public interface BookClubRepo extends JpaRepository<BookClub, UUID> {
    @NotNull Page<BookClub> findAll(@NotNull Pageable pageable);

    Optional<BookClub> findByName(final String name);

    @Query(
            nativeQuery = true,
            value = "SELECT bc.* FROM book_club bc "
                    + "INNER JOIN book_club_users bcr "
                    + "ON bc.id = bcr.book_club_id "
                    + "INNER JOIN app_user r "
                    + "ON bcr.user_id = r.id "
                    + "WHERE bc.disbanded IS NULL "
                    + "AND bcr.departed IS NULL "
                    + "AND r.id = :userId")
    Page<BookClub> findAllForUser(final UUID userId, Pageable pageable);

    Page<BookClub> findAllByPublicityNotAndNameContainsIgnoreCase(
            final Publicity publicity, final String searchTerm, Pageable pageable);

    @Query(
            nativeQuery = true,
            value = "SELECT bc.* FROM book_club bc "
                    + "INNER JOIN book_club_users bcu "
                    + "ON bc.id = bcu.book_club_id "
                    + "INNER JOIN app_user r "
                    + "ON bcu.user_id = r.id "
                    + "WHERE bc.id = :id "
                    + "AND r.id = :userID "
                    + "AND bc.disbanded IS NULL "
                    + "AND bcu.departed IS NULL "
                    + "AND r.departed IS NULL "
                    + "AND bcu.club_role = 'ADMIN'")
    Optional<BookClub> findByIdAndUserIsAdmin(final UUID id, final UUID userID);
}
