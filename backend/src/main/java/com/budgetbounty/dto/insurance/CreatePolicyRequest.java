package com.budgetbounty.dto.insurance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreatePolicyRequest {
    @NotBlank
    private String provider;

    @NotBlank
    private String policyNumber;

    private String policyType;

    @Positive
    private Double premiumAmount;

    @NotNull
    private LocalDate dueDate;
}
