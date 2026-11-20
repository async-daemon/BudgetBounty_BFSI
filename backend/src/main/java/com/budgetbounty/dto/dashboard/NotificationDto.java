package com.budgetbounty.dto.dashboard;

import com.budgetbounty.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String category;
    private String icon;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getCategory(), n.getIcon(),
                Boolean.TRUE.equals(n.getIsRead()), n.getCreatedAt());
    }
}
