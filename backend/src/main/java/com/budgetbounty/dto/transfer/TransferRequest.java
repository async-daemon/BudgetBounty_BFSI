package com.budgetbounty.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

    @NotBlank
    private String method; // UPI | NEFT | RTGS | IMPS

    @NotBlank
    private String recipientName;

    @NotBlank
    private String recipientHandle;

    @Positive
    private Double amount;

    private String note;
}
