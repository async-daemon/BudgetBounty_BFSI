package com.budgetbounty.controller;

import com.budgetbounty.dto.locker.DocumentDto;
import com.budgetbounty.entity.Document;
import com.budgetbounty.security.UserPrincipal;
import com.budgetbounty.service.LockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/locker")
@RequiredArgsConstructor
public class LockerController {

    private final LockerService lockerService;

    @GetMapping
    public ResponseEntity<List<DocumentDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(lockerService.listForUser(principal.getId()));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentDto> upload(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "folder", required = false) String folder) {
        return ResponseEntity.ok(lockerService.upload(principal.getId(), file, folder));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable Long id) {
        Document document = lockerService.getDocument(principal.getId(), id);
        Resource resource = lockerService.download(principal.getId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        lockerService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
