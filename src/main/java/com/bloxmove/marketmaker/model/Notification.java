package com.bloxmove.marketmaker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Document
@Data
public class Notification {

    @Id
    @NotNull
    private String id;
    @NotNull
    private NotificationType type;
    @NotNull
    private NotificationStatus status;
    @NotNull
    private String message;
    @NotNull
    private Instant created;
    private Instant updated;

    public Notification id(String id) {
        this.id = id;
        return this;
    }

    public Notification type(NotificationType type) {
        this.type = type;
        return this;
    }

    public Notification status(NotificationStatus status) {
        this.status = status;
        return this;
    }

    public Notification message(String message) {
        this.message = message;
        return this;
    }

    public Notification created(Instant created) {
        this.created = created;
        return this;
    }

    public Notification updated(Instant updated) {
        this.updated = updated;
        return this;
    }

}
