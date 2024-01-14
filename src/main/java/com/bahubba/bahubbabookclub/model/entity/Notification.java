package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Notification entity */
@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_user_id")
    @NotNull private User sourceUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_user_id")
    @NotNull private User targetUser;

    @ManyToOne
    @JoinColumn(name = "book_club_id")
    private BookClub bookClub;

    @Column(nullable = false)
    @NotNull @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "action_link")
    private String actionLink;

    @Column(nullable = false)
    @NotNull @Builder.Default
    private LocalDateTime generated = LocalDateTime.now();

    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY)
    private Set<NotificationViews> views;
}
