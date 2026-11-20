package com.budgetbounty.controller;

import com.budgetbounty.dto.property.*;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<List<PropertyDto>> listProperties(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(propertyService.listProperties(principal.getId()));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PropertyPaymentDto>> listPayments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(propertyService.listPayments(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<PropertyDto> addProperty(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody CreatePropertyRequest request) {
        return ResponseEntity.ok(propertyService.addProperty(principal.getId(), request));
    }

    @PostMapping("/{propertyId}/schedule-payment")
    public ResponseEntity<PropertyPaymentDto> schedule(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long propertyId,
                                                        @Valid @RequestBody SchedulePaymentRequest request) {
        return ResponseEntity.ok(propertyService.schedulePayment(principal.getId(), propertyId, request));
    }

    @PostMapping("/payments/{paymentId}/pay")
    public ResponseEntity<PropertyPaymentDto> pay(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable Long paymentId) {
        return ResponseEntity.ok(propertyService.pay(principal.getId(), paymentId));
    }
}
