package com.budgetbounty.dto.property;

import com.budgetbounty.entity.PropertyPayment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PropertyPaymentDto {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private String paymentType;
    private Double amount;
    private LocalDate dueDate;
    private String status;
    private Integer rewardPointsEarned;
    private LocalDateTime paidAt;

    public static PropertyPaymentDto from(PropertyPayment p) {
        return new PropertyPaymentDto(p.getId(), p.getProperty().getId(), p.getProperty().getName(),
                p.getPaymentType(), p.getAmount(), p.getDueDate(), p.getStatus(), p.getRewardPointsEarned(),
                p.getPaidAt());
    }
}
