package com.budgetbounty.config;

import com.budgetbounty.entity.*;
import com.budgetbounty.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Idempotent startup seed: default admin reward rules + reward catalog
 * (always), and demo admin/user accounts with sample data across every
 * module (only on a first run, when no users exist yet). Mirrors the Flask
 * build's app/__init__.py _seed_defaults() + seed.py.
 */
@Component
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final AdminRewardRuleRepository rewardRuleRepository;
    private final RewardCatalogItemRepository catalogItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LinkedAccountRepository linkedAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final InvestmentGoalRepository investmentGoalRepository;
    private final InvestmentRepository investmentRepository;
    private final PropertyAssetRepository propertyAssetRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final TaxRecordRepository taxRecordRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRewardRules();
        seedRewardCatalog();
        if (userRepository.count() == 0) {
            seedDemoUsers();
        }
    }

    private void seedRewardRules() {
        if (rewardRuleRepository.count() > 0) {
            return;
        }
        rewardRuleRepository.saveAll(java.util.List.of(
                AdminRewardRule.builder().module("transfer").label("Digital Money Transfer (per ₹100)")
                        .pointsValue(1).active(true).build(),
                AdminRewardRule.builder().module("budget_adherence").label("Stayed Within Budget")
                        .pointsValue(50).active(true).build(),
                AdminRewardRule.builder().module("bill_timely").label("Timely Bill Payment")
                        .pointsValue(25).active(true).build(),
                AdminRewardRule.builder().module("investment_goal").label("Investment Goal Achieved")
                        .pointsValue(75).active(true).build(),
                AdminRewardRule.builder().module("property_payment").label("Property Payment")
                        .pointsValue(20).active(true).build(),
                AdminRewardRule.builder().module("insurance_payment").label("Insurance Premium Payment")
                        .pointsValue(30).active(true).build(),
                AdminRewardRule.builder().module("tax_payment").label("Timely Tax Payment")
                        .pointsValue(40).active(true).build()
        ));
    }

    private void seedRewardCatalog() {
        if (catalogItemRepository.count() > 0) {
            return;
        }
        catalogItemRepository.saveAll(java.util.List.of(
                RewardCatalogItem.builder().name("Amazon Gift Card - ₹500")
                        .description("Redeemable on amazon.in").category("giftcard").partnerName("Amazon")
                        .pointsRequired(2000).icon("fa-gift").active(true).build(),
                RewardCatalogItem.builder().name("Flipkart Gift Card - ₹250")
                        .description("Redeemable on flipkart.com").category("giftcard").partnerName("Flipkart")
                        .pointsRequired(1000).icon("fa-gift").active(true).build(),
                RewardCatalogItem.builder().name("Movie Ticket Discount")
                        .description("Flat ₹100 off on BookMyShow").category("discount")
                        .partnerName("BookMyShow").pointsRequired(400).icon("fa-ticket").active(true).build(),
                RewardCatalogItem.builder().name("Electricity Bill Cashback ₹100")
                        .description("Cashback credited to linked account").category("cashback")
                        .partnerName("BudgetBounty").pointsRequired(500).icon("fa-bolt").active(true).build()
        ));
    }

    private void seedDemoUsers() {
        User admin = User.builder()
                .name("Admin User").email("admin@budgetbounty.app")
                .passwordHash(passwordEncoder.encode("Admin@123")).role("admin").rewardPoints(0).build();
        userRepository.save(admin);

        User demo = User.builder()
                .name("Demo User").email("demo@budgetbounty.app").phone("9876543210")
                .passwordHash(passwordEncoder.encode("Demo@123")).role("user").rewardPoints(320).build();
        userRepository.save(demo);

        linkedAccountRepository.save(LinkedAccount.builder().user(demo).institutionName("HDFC Bank")
                .accountType("bank").accountNumberMasked("XXXX1234").balance(62374.0).status("active").build());
        linkedAccountRepository.save(LinkedAccount.builder().user(demo).institutionName("Google Pay")
                .accountType("upi").upiId("demo@okhdfcbank").balance(0.0).status("active").build());

        transactionRepository.save(Transaction.builder().user(demo).referenceNo("BB1000000001").method("UPI")
                .recipientName("Ravi Kumar").recipientHandle("ravi@okaxis").amount(1500.0).status("success")
                .rewardPointsEarned(15).build());
        transactionRepository.save(Transaction.builder().user(demo).referenceNo("BB1000000002").method("NEFT")
                .recipientName("Landlord").recipientHandle("HDFC0001234/44556677").amount(18000.0)
                .status("success").rewardPointsEarned(180).build());

        Budget groceries = Budget.builder().user(demo).category("Groceries").monthlyLimit(8000.0)
                .spentAmount(5200.0).period(LocalDate.now().toString().substring(0, 7)).build();
        budgetRepository.save(groceries);
        Budget dining = Budget.builder().user(demo).category("Dining Out").monthlyLimit(4000.0)
                .spentAmount(4500.0).period(LocalDate.now().toString().substring(0, 7)).build();
        budgetRepository.save(dining);

        billRepository.save(Bill.builder().user(demo).name("Electricity Bill").category("Electricity")
                .amount(2200.0).dueDate(LocalDate.now().plusDays(5)).recurring(true).frequency("monthly")
                .build());
        billRepository.save(Bill.builder().user(demo).name("Internet Bill").category("Internet")
                .amount(999.0).dueDate(LocalDate.now().plusDays(10)).recurring(true).frequency("monthly")
                .build());

        InvestmentGoal goal = InvestmentGoal.builder().user(demo).name("Emergency Fund")
                .targetAmount(200000.0).targetDate(LocalDate.now().plusMonths(12)).build();
        investmentGoalRepository.save(goal);
        investmentRepository.save(Investment.builder().user(demo).goal(goal).name("HDFC Balanced Fund")
                .assetType("Mutual Fund").investedAmount(50000.0).currentValue(54300.0).build());
        investmentRepository.save(Investment.builder().user(demo).name("SGB Gold Bonds")
                .assetType("Gold").investedAmount(20000.0).currentValue(21800.0).build());

        PropertyAsset property = PropertyAsset.builder().user(demo).name("Sunrise Apartments, 4B")
                .address("Pune, Maharashtra").propertyManager("Urban Estates").build();
        propertyAssetRepository.save(property);
        propertyPaymentRepository.save(PropertyPayment.builder().property(property).paymentType("Rent")
                .amount(18000.0).dueDate(LocalDate.now().plusDays(3)).status("pending").build());

        insurancePolicyRepository.save(InsurancePolicy.builder().user(demo).provider("LIC")
                .policyNumber("LIC-998877").policyType("Life").premiumAmount(12000.0)
                .dueDate(LocalDate.now().plusDays(15)).status("active").build());

        taxRecordRepository.save(TaxRecord.builder().user(demo).assessmentYear("2026-27")
                .taxType("Income Tax").amountDue(45000.0).dueDate(LocalDate.now().plusDays(30))
                .status("pending").build());
    }
}
