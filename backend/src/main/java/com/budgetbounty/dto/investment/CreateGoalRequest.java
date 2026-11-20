package com.budgetbounty.dto.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateGoalRequest {
    @NotBlank
    private String name;

    @Positive
    private Double targetAmount;

    private LocalDate targetDate;
}
