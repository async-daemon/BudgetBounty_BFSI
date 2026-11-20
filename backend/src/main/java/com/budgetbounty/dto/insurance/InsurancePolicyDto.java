package com.budgetbounty.dto.insurance;

import com.budgetbounty.entity.InsurancePolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class InsurancePolicyDto {
    private Long id;
    private String provider;
    private String policyNumber;
    private String policyType;
    private Double premiumAmount;
    private LocalDate dueDate;
    private String status;

    public static InsurancePolicyDto from(InsurancePolicy p) {
        return new InsurancePolicyDto(p.getId(), p.getProvider(), p.getPolicyNumber(), p.getPolicyType(),
                p.getPremiumAmount(), p.getDueDate(), p.getStatus());
    }
}
