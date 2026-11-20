package com.budgetbounty.dto.dashboard;

import com.budgetbounty.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LinkedAccountDto {
    private Long id;
    private String institutionName;
    private String accountType;
    private String accountNumberMasked;
    private String upiId;
    private Double balance;
    private String status;
    private LocalDateTime linkedAt;

    public static LinkedAccountDto from(LinkedAccount a) {
        return new LinkedAccountDto(a.getId(), a.getInstitutionName(), a.getAccountType(),
                a.getAccountNumberMasked(), a.getUpiId(), a.getBalance(), a.getStatus(), a.getLinkedAt());
    }
}
