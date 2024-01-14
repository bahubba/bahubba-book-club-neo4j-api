package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for the {@link Notification} entity */
public interface NotificationRepo extends JpaRepository<Notification, UUID> {}
