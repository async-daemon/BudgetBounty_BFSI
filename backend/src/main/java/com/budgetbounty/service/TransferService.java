package com.budgetbounty.service;

import com.budgetbounty.dto.transfer.TransactionDto;
import com.budgetbounty.dto.transfer.TransferRequest;
import com.budgetbounty.entity.Transaction;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.TransactionRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public List<TransactionDto> listForUser(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(TransactionDto::from).toList();
    }

    @Transactional
    public TransactionDto createTransfer(Long userId, TransferRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        int pointsPer100 = rewardService.getRulePoints("transfer", 1);
        int pointsEarned = (int) (Math.floor(request.getAmount() / 100) * pointsPer100);

        Transaction tx = Transaction.builder()
                .user(user)
                .referenceNo(generateReference())
                .method(request.getMethod())
                .recipientName(request.getRecipientName())
                .recipientHandle(request.getRecipientHandle())
                .amount(request.getAmount())
                .note(request.getNote())
                .status("success")
                .rewardPointsEarned(pointsEarned)
                .build();
        transactionRepository.save(tx);

        if (pointsEarned > 0) {
            rewardService.awardPoints(user, pointsEarned, "transfer",
                    request.getMethod() + " payment of ₹" + String.format("%,.0f", request.getAmount())
                            + " to " + request.getRecipientName(), false);
        }

        rewardService.notifyUser(user, "Payment successful",
                "₹" + String.format("%,.0f", request.getAmount()) + " sent to " + request.getRecipientName()
                        + " via " + request.getMethod() + ". Ref: " + tx.getReferenceNo(),
                "success", "fa-circle-check");

        return TransactionDto.from(tx);
    }

    private String generateReference() {
        StringBuilder sb = new StringBuilder("BB");
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
