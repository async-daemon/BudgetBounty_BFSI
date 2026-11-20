package com.budgetbounty.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardOverviewDto {
    private Integer rewardPoints;
    private double totalTransferred;
    private double totalBudgetSpent;
    private long upcomingBillsCount;
    private double investmentCurrentValue;
    private long pendingPropertyPayments;
    private long activeInsurancePolicies;
    private long pendingTaxRecords;
    private long documentsCount;
    private long unreadNotifications;
}
