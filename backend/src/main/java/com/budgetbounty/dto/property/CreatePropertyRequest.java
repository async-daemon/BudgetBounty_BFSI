package com.budgetbounty.dto.property;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePropertyRequest {
    @NotBlank
    private String name;

    private String address;
    private String manager;
}
