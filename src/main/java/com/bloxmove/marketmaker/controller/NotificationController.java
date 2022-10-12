package com.bloxmove.marketmaker.controller;

import com.bloxmove.marketmaker.model.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Notification API")
@CrossOrigin
@RequestMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE)
public interface NotificationController {

    @Operation(summary = "Close notification")
    @PutMapping(value = "/{notificationId}")
    void close(@PathVariable String notificationId);

    @Operation(summary = "Get notifications")
    @GetMapping
    List<Notification> get(@Parameter(name = "status",
            description = "Notification status", required = true) String status);
}
