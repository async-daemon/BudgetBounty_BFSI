package com.budgetbounty.dto.tax;

import com.budgetbounty.entity.TaxRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaxRecordDto {
    private Long id;
    private String assessmentYear;
    private String taxType;
    private Double amountDue;
    private LocalDate dueDate;
    private String status;
    private Integer rewardPointsEarned;
    private LocalDateTime paidAt;

    public static TaxRecordDto from(TaxRecord r) {
        return new TaxRecordDto(r.getId(), r.getAssessmentYear(), r.getTaxType(), r.getAmountDue(), r.getDueDate(),
                r.getStatus(), r.getRewardPointsEarned(), r.getPaidAt());
    }
}
