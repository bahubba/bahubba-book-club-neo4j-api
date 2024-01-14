package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.*;

/**
 * Book Clubs, or discussion groups for books, which can be created, joined, managed, etc. Heart of
 * the application
 */
@Entity
@Table(name = "book_club")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClub implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotNull private String name;

    @Column(name = "image_file_name", nullable = false)
    private String imageFileName;

    @Column(nullable = false)
    @NotNull @Builder.Default
    private String description = "A book club for reading books!";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull @Builder.Default
    private Publicity publicity = Publicity.PRIVATE;

    @OneToMany(mappedBy = "bookClub", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<BookClubMembership> members;

    @Column(nullable = false)
    @NotNull @Builder.Default
    private LocalDateTime created = LocalDateTime.now();

    @Column
    private LocalDateTime disbanded;
}
