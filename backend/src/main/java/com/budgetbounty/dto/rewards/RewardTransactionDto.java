package com.budgetbounty.dto.rewards;

import com.budgetbounty.entity.RewardTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RewardTransactionDto {
    private Long id;
    private Integer points;
    private String entryType;
    private String sourceModule;
    private String description;
    private LocalDateTime createdAt;

    public static RewardTransactionDto from(RewardTransaction t) {
        return new RewardTransactionDto(t.getId(), t.getPoints(), t.getEntryType(), t.getSourceModule(),
                t.getDescription(), t.getCreatedAt());
    }
}
