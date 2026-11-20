package com.budgetbounty.dto.dashboard;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkAccountRequest {
    @NotBlank
    private String institutionName;

    @NotBlank
    private String accountType; // bank | upi | investment | insurance | tax | property

    private String accountNumberMasked;
    private String upiId;
}
