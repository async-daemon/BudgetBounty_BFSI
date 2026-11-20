package com.budgetbounty.dto.property;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SchedulePaymentRequest {
    private String paymentType;

    @Positive
    private Double amount;

    @NotNull
    private LocalDate dueDate;
}
