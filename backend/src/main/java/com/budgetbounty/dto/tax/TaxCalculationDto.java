package com.budgetbounty.dto.tax;

import com.budgetbounty.entity.TaxCalculation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaxCalculationDto {
    private Long id;
    private Double annualIncome;
    private String regime;
    private Double deductions;
    private Double taxableIncome;
    private Double taxPayable;
    private Double cess;
    private Double totalPayable;

    public static TaxCalculationDto from(TaxCalculation c) {
        return new TaxCalculationDto(c.getId(), c.getAnnualIncome(), c.getRegime(), c.getDeductions(),
                c.getTaxableIncome(), c.getTaxPayable(), c.getCess(), c.getTotalPayable());
    }
}
