package com.budgetbounty.controller;

import com.budgetbounty.dto.bill.BillDto;
import com.budgetbounty.dto.bill.CreateBillRequest;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<List<BillDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billService.listForUser(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<BillDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.ok(billService.create(principal.getId(), request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<BillDto> pay(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(billService.pay(principal.getId(), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BillDto> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(billService.cancel(principal.getId(), id));
    }
}
