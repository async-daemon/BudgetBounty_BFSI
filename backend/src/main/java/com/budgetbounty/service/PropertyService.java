package com.budgetbounty.service;

import com.budgetbounty.dto.property.CreatePropertyRequest;
import com.budgetbounty.dto.property.PropertyDto;
import com.budgetbounty.dto.property.PropertyPaymentDto;
import com.budgetbounty.dto.property.SchedulePaymentRequest;
import com.budgetbounty.entity.PropertyAsset;
import com.budgetbounty.entity.PropertyPayment;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.BadRequestException;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.PropertyAssetRepository;
import com.budgetbounty.repository.PropertyPaymentRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyAssetRepository propertyAssetRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public List<PropertyDto> listProperties(Long userId) {
        return propertyAssetRepository.findByUserId(userId).stream().map(PropertyDto::from).toList();
    }

    public List<PropertyPaymentDto> listPayments(Long userId) {
        return propertyPaymentRepository.findAllForUser(userId).stream().map(PropertyPaymentDto::from).toList();
    }

    @Transactional
    public PropertyDto addProperty(Long userId, CreatePropertyRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        PropertyAsset property = PropertyAsset.builder()
                .user(user)
                .name(request.getName())
                .address(request.getAddress())
                .propertyManager(request.getManager())
                .build();
        propertyAssetRepository.save(property);
        return PropertyDto.from(property);
    }

    @Transactional
    public PropertyPaymentDto schedulePayment(Long userId, Long propertyId, SchedulePaymentRequest request) {
        PropertyAsset property = propertyAssetRepository.findById(propertyId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Property not found"));

        PropertyPayment payment = PropertyPayment.builder()
                .property(property)
                .paymentType(request.getPaymentType() == null ? "Rent" : request.getPaymentType())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .build();
        propertyPaymentRepository.save(payment);
        return PropertyPaymentDto.from(payment);
    }

    @Transactional
    public PropertyPaymentDto pay(Long userId, Long paymentId) {
        PropertyPayment payment = propertyPaymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!"approved".equals(payment.getStatus())) {
            throw new BadRequestException("Payment must be approved by an administrator before it can be paid");
        }

        payment.setStatus("paid");
        payment.setPaidAt(LocalDateTime.now());

        int points = rewardService.getRulePoints("property_payment", 20);
        rewardService.awardPoints(payment.getProperty().getUser(), points, "property_payment",
                payment.getPaymentType() + " payment for " + payment.getProperty().getName());
        payment.setRewardPointsEarned(points);

        propertyPaymentRepository.save(payment);
        return PropertyPaymentDto.from(payment);
    }
}
