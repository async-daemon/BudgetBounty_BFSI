package com.budgetbounty.dto.rewards;

import com.budgetbounty.entity.Redemption;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RedemptionDto {
    private Long id;
    private String itemName;
    private Integer pointsSpent;
    private String status;
    private LocalDateTime redeemedAt;

    public static RedemptionDto from(Redemption r) {
        return new RedemptionDto(r.getId(), r.getItem().getName(), r.getPointsSpent(), r.getStatus(),
                r.getRedeemedAt());
    }
}
