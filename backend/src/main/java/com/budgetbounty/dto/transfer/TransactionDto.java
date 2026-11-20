package com.budgetbounty.dto.transfer;

import com.budgetbounty.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String referenceNo;
    private String method;
    private String recipientName;
    private String recipientHandle;
    private Double amount;
    private String note;
    private String status;
    private Integer rewardPointsEarned;
    private LocalDateTime createdAt;

    public static TransactionDto from(Transaction t) {
        return new TransactionDto(t.getId(), t.getReferenceNo(), t.getMethod(), t.getRecipientName(),
                t.getRecipientHandle(), t.getAmount(), t.getNote(), t.getStatus(), t.getRewardPointsEarned(),
                t.getCreatedAt());
    }
}
