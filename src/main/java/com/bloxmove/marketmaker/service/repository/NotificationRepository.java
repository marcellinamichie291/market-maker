package com.bloxmove.marketmaker.service.repository;

import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByStatus(NotificationStatus status);
}
