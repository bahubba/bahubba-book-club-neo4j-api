package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Requests from Users (users) for book club membership */
@Entity
@Table(name = "membership_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_club_id", referencedColumnName = "id")
    private BookClub bookClub;

    @Column
    private String message;

    @Column
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.OPEN;

    @Column
    @Enumerated(EnumType.STRING)
    private BookClubRole role;

    @Column
    @Builder.Default
    private Boolean viewed = false;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id")
    private User reviewer;

    @Column(name = "review_message")
    private String reviewMessage;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime requested = LocalDateTime.now();

    @Column
    private LocalDateTime reviewed;
}
