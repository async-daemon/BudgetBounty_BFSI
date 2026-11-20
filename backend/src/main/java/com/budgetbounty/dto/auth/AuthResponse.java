package com.budgetbounty.dto.auth;

import com.budgetbounty.dto.common.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
}
