package com.budgetbounty.service;

import com.budgetbounty.dto.tax.*;
import com.budgetbounty.entity.TaxCalculation;
import com.budgetbounty.entity.TaxRecord;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.TaxCalculationRepository;
import com.budgetbounty.repository.TaxRecordRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Mirrors the Flask build's app/blueprints/tax/routes.py: same illustrative
 * Indian new/old-regime slab rates (FY2023-24 style), same standard
 * deduction and cess handling.
 */
@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxRecordRepository taxRecordRepository;
    private final TaxCalculationRepository taxCalculationRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    private static final double[][] NEW_REGIME_SLABS = {
            {300000, 0.0}, {300000, 0.05}, {300000, 0.10}, {300000, 0.15}, {300000, 0.20}, {Double.MAX_VALUE, 0.30}
    };

    private static final double[][] OLD_REGIME_SLABS = {
            {250000, 0.0}, {250000, 0.05}, {500000, 0.20}, {Double.MAX_VALUE, 0.30}
    };

    public List<TaxRecordDto> listRecords(Long userId) {
        return taxRecordRepository.findByUserIdOrderByDueDateAsc(userId).stream().map(TaxRecordDto::from).toList();
    }

    public List<TaxCalculationDto> recentCalculations(Long userId) {
        return taxCalculationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5).map(TaxCalculationDto::from).toList();
    }

    @Transactional
    public TaxCalculationDto calculate(Long userId, CalculateTaxRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        double annualIncome = request.getAnnualIncome();
        double deductions = request.getDeductions() == null ? 0 : request.getDeductions();
        String regime = "old".equals(request.getRegime()) ? "old" : "new";

        double taxableIncome;
        double taxPayable;
        if ("new".equals(regime)) {
            taxableIncome = Math.max(annualIncome - 50000, 0);
            taxPayable = slabTax(taxableIncome, NEW_REGIME_SLABS);
        } else {
            taxableIncome = Math.max(annualIncome - deductions - 50000, 0);
            taxPayable = slabTax(taxableIncome, OLD_REGIME_SLABS);
        }

        double cess = taxPayable * 0.04;
        double totalPayable = taxPayable + cess;

        TaxCalculation calc = TaxCalculation.builder()
                .user(user)
                .annualIncome(annualIncome)
                .regime(regime)
                .deductions(deductions)
                .taxableIncome(taxableIncome)
                .taxPayable(taxPayable)
                .cess(cess)
                .totalPayable(totalPayable)
                .build();
        taxCalculationRepository.save(calc);

        return TaxCalculationDto.from(calc);
    }

    private double slabTax(double taxableIncome, double[][] slabs) {
        double remaining = taxableIncome;
        double tax = 0.0;
        for (double[] slab : slabs) {
            if (remaining <= 0) {
                break;
            }
            double width = slab[0];
            double rate = slab[1];
            double amountInSlab = Math.min(remaining, width);
            tax += amountInSlab * rate;
            remaining -= amountInSlab;
        }
        return tax;
    }

    @Transactional
    public TaxRecordDto addRecord(Long userId, CreateTaxRecordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        String taxType = request.getTaxType() == null ? "Income Tax" : request.getTaxType();

        TaxRecord record = TaxRecord.builder()
                .user(user)
                .assessmentYear(request.getAssessmentYear())
                .taxType(taxType)
                .amountDue(request.getAmountDue())
                .dueDate(request.getDueDate())
                .build();
        taxRecordRepository.save(record);

        rewardService.notifyUser(user, "Tax account linked",
                taxType + " for AY " + request.getAssessmentYear() + " due " + request.getDueDate(),
                "info", "fa-file-invoice-dollar");

        return TaxRecordDto.from(record);
    }

    @Transactional
    public TaxRecordDto payRecord(Long userId, Long recordId) {
        TaxRecord record = taxRecordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new NotFoundException("Tax record not found"));
        boolean wasTimely = !record.getDueDate().isBefore(LocalDate.now());

        record.setStatus("paid");
        record.setPaidAt(java.time.LocalDateTime.now());

        if (wasTimely) {
            int points = rewardService.getRulePoints("tax_payment", 40);
            rewardService.awardPoints(record.getUser(), points, "tax_payment",
                    "Timely " + record.getTaxType() + " payment for AY " + record.getAssessmentYear());
            record.setRewardPointsEarned(points);
        }

        taxRecordRepository.save(record);
        return TaxRecordDto.from(record);
    }
}
