package com.budgetbounty.dto.tax;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTaxRecordRequest {
    @NotBlank
    private String assessmentYear;

    private String taxType;

    @Positive
    private Double amountDue;

    @NotNull
    private LocalDate dueDate;
}
