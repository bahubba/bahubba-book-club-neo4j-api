package com.bahubba.bahubbabookclub.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A composite key table to track which notifications a user has viewed */
@Entity
@Table(name = "notification_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViews implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "notification_id")
    @NotNull private Notification notification;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @NotNull private User user;
}
