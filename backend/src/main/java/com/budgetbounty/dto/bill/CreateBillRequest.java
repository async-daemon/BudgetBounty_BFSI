package com.budgetbounty.dto.bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateBillRequest {
    @NotBlank
    private String name;

    private String category;

    @Positive
    private Double amount;

    @NotNull
    private LocalDate dueDate;

    private boolean recurring;
    private String frequency;
    private boolean autopay;
}
