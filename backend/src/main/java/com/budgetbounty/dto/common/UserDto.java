package com.budgetbounty.dto.common;

import com.budgetbounty.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Integer rewardPoints;
    private String initials;
    private String avatarColor;
    private boolean admin;

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getRewardPoints(),
                user.getInitials(),
                user.getAvatarColor(),
                user.isAdmin()
        );
    }
}
