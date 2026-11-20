package com.budgetbounty.controller;

import com.budgetbounty.dto.transfer.TransactionDto;
import com.budgetbounty.dto.transfer.TransferRequest;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public ResponseEntity<List<TransactionDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(transferService.listForUser(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.createTransfer(principal.getId(), request));
    }
}
