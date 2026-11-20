package com.budgetbounty.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String confirmPassword;
}
