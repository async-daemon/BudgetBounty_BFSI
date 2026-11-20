package com.budgetbounty.controller;

import com.budgetbounty.dto.tax.*;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.TaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;

    @GetMapping("/records")
    public ResponseEntity<List<TaxRecordDto>> listRecords(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taxService.listRecords(principal.getId()));
    }

    @GetMapping("/calculations")
    public ResponseEntity<List<TaxCalculationDto>> recentCalculations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taxService.recentCalculations(principal.getId()));
    }

    @PostMapping("/calculator")
    public ResponseEntity<TaxCalculationDto> calculate(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody CalculateTaxRequest request) {
        return ResponseEntity.ok(taxService.calculate(principal.getId(), request));
    }

    @PostMapping("/records")
    public ResponseEntity<TaxRecordDto> addRecord(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody CreateTaxRecordRequest request) {
        return ResponseEntity.ok(taxService.addRecord(principal.getId(), request));
    }

    @PostMapping("/records/{id}/pay")
    public ResponseEntity<TaxRecordDto> payRecord(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(taxService.payRecord(principal.getId(), id));
    }
}
