package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Membership of {@link User}s (users) in {@link BookClub}s TODO - Utilize composite key instead
 * of having a dedicated id
 */
@Entity
@Table(name = "book_club_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubMembership implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "book_club_id", referencedColumnName = "id")
    private BookClub bookClub;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "club_role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookClubRole clubRole = BookClubRole.USER;

    @Column(name = "is_owner", nullable = false)
    @Builder.Default
    private boolean isOwner = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joined = LocalDateTime.now();

    @Column
    private LocalDateTime departed;
}
