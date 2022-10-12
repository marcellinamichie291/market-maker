package com.bloxmove.marketmaker.controller.impl;

import com.bloxmove.marketmaker.controller.NotificationController;
import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RequestMapping(value = "/notification")
@RestController
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {

    private final NotificationRepository notificationRepository;

    @Override
    public void close(String notificationId) {
        Notification notificationFromDb = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("There is no notification with id %s", notificationId)));
        notificationRepository.save(notificationFromDb
                .status(NotificationStatus.CLOSED)
                .updated(Instant.now()));
    }

    @Override
    public List<Notification> get(String status) {
        return notificationRepository.findByStatus(NotificationStatus.valueOf(status));
    }
}
