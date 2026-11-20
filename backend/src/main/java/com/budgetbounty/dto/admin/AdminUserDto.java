package com.budgetbounty.dto.admin;

import com.budgetbounty.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Integer rewardPoints;
    private LocalDateTime createdAt;

    public static AdminUserDto from(User u) {
        return new AdminUserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getRewardPoints(),
                u.getCreatedAt());
    }
}
